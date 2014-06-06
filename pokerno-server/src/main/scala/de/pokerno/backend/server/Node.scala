package de.pokerno.backend.server

import org.slf4j.{ Logger, LoggerFactory }

import de.pokerno.model.{Variation, Stake}
import de.pokerno.model.Seat.{State => SeatState}
import de.pokerno.backend.{ gateway ⇒ gw }
import de.pokerno.backend.Gateway
import de.pokerno.backend.gateway.http
import de.pokerno.protocol._
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.twitter.util.Future
import org.apache.thrift.protocol.TBinaryProtocol

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, ActorSystem }

object Node {

  val log = LoggerFactory.getLogger(getClass)
  lazy val system = ActorSystem("node")

  def start(config: Config): ActorRef = {
    log.info("starting with config: {}", config)

    log.info("starting node {}", config.host)
    val node = system.actorOf(Props(classOf[Node]), name = f"node-${config.host}")

    config.rpc.map { rpcConfig ⇒
      log.info("starting rpc with config: {}", rpcConfig)
    }

    config.http.map { httpConfig ⇒
      log.info("starting HTTP gateway")
      val httpGateway = system.actorOf(Props(classOf[gw.Http.Gateway], Some(node)), name = "http-gateway")

      log.info("starting HTTP server with config: {}", httpConfig)
      val server = new gw.http.Server(httpGateway, httpConfig)
      server.start
    }

    node
  }

  import com.fasterxml.jackson.annotation.JsonProperty
  
  case class CreateRoom(
    @JsonProperty id: String,
    @JsonProperty variation: Variation,
    @JsonProperty stake: Stake
  )
  
  case class ChangeRoomState(
    id: String,
    newState: Room.ChangeState
  )
  
  class Service extends thrift.rpc.Node.FutureIface {
    def createRoom(id: String,
        variation: thrift.Variation,
        stake: thrift.Stake,
        table: thrift.Table): Future[Unit] = {
      Future.value(())
    }
    
    def maintenance: Future[Unit] = Future.value(())
  }
  
  object Service {
    def apply(addr: java.net.InetSocketAddress) = {
      Thrift.serve[thrift.rpc.Node.FinagledService, thrift.rpc.Node.FutureIface](new Service, "NodeService", addr)
    }
  }
}

class Node extends Actor with ActorLogging {
  import context._
  import concurrent.duration._
  import util.{ Success, Failure }

  override def preStart {
  }

  def receive = {
    // new connection
    case Gateway.Connect(conn) if conn.room.isDefined ⇒
      val id = conn.room.get

      actorSelection(id).resolveOne(1 second).onComplete {
        case Success(room) ⇒
          room ! Room.Connect(conn)

        case Failure(_) ⇒
          log.warning("room {} not found for conn {}", id, conn)
          conn.close()
      }

    case Gateway.Disconnect(conn) if conn.room.isDefined ⇒
      val id = conn.room.get

      actorSelection(id).resolveOne(1 second).onComplete {
        case Success(room) ⇒
          room ! Room.Disconnect(conn)

        case Failure(_) ⇒
          log.warning("room {} not found for conn {}", id, conn)
      }

    // catch player messages
    case Gateway.Message(conn, msg) if conn.player.isDefined && conn.room.isDefined ⇒
      val player = conn.player.get
      val id = conn.room.get

      actorSelection(id).resolveOne(1 second).onComplete {
        case Success(room) ⇒
          room ! (msg match {
            case join: action.JoinTable ⇒
              cmd.JoinPlayer(join.pos, player, join.amount)

            case leave: action.LeaveTable ⇒
              cmd.KickPlayer(player)
            
            case sitOut: action.SitOut =>
              cmd.ChangePlayerState(player, SeatState.Idle)
            
            case comeBack: action.ComeBack =>
              cmd.ChangePlayerState(player, SeatState.Ready)

            case add: action.AddBet ⇒
              cmd.AddBet(player, add.bet)
          })

        case Failure(_) ⇒
          log.warning("Room not found: {}", id)
      }

    case Node.CreateRoom(id, variation, stake) ⇒
      actorSelection(id).resolveOne(1 second).onComplete {
        case Failure(_) ⇒
          log.info("spawning new room with id={}", id)
          val room = context.actorOf(Props(classOf[Room], id, variation, stake), name = id)
          room
        case _ ⇒
          log.warning("Room exists: {}", id)
      }

    case Node.ChangeRoomState(id, newState) ⇒
      actorSelection(id).resolveOne(1 second).onComplete {
        case Success(room) ⇒
          room ! newState
        case Failure(_) ⇒
          log.warning("Room not found: {}", id)
      }

    case x ⇒
      log.warning("unhandled: {}", x)
  }

  override def postStop {
  }
}
