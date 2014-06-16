package de.pokerno.backend.server

import org.slf4j.{ Logger, LoggerFactory }

import de.pokerno.model.{Variation, Stake}
import de.pokerno.model.Seat.{State => SeatState}
import de.pokerno.backend.{ gateway ⇒ gw }
import de.pokerno.backend.Gateway
import de.pokerno.backend.gateway.http
import de.pokerno.protocol._

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, ActorSystem }

object Node {

  val log = LoggerFactory.getLogger(getClass)
  implicit val system = ActorSystem("node")
  
  def start(config: Config): ActorRef = {
    log.info("starting with config: {}", config)

    log.info("starting node at {}", config.host)
    val node = system.actorOf(Props(classOf[Node]), name = "node-main")

    config.rpc.map { rpcConfig ⇒
      log.info("starting rpc with config: {}", rpcConfig)
      Service(node, new java.net.InetSocketAddress(rpcConfig.host, rpcConfig.port))
    }
    
    config.http.map { httpConfig ⇒
      val httpGateway = system.actorOf(Props(classOf[gw.Http.Gateway], Some(node)), name = "http-gateway")

      log.info("starting HTTP server with config: {}", httpConfig)
      val server = new gw.http.Server(httpGateway, httpConfig)
      server.start
    }
    
    config.api.map { apiConfig =>
      import spray.can.Http
      val httpApi = system.actorOf(Props(classOf[Api]), name = "http-api")
      akka.io.IO(Http) ! Http.Bind(httpApi, config.host, port = apiConfig.port)
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
  
  case class SendCommand(
    id: String,
    command: cmd.Command
  )
  
}

class Node extends Actor with ActorLogging {
  import context._
  import concurrent.duration._
  import util.{ Success, Failure }
  import CommandConversions._

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
      implicit val player = conn.player.get
      val id = conn.room.get

      actorSelection(id).resolveOne(1 second).onComplete {
        case Success(room) ⇒
          room ! (msg: Command)

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
    
    case Node.SendCommand(id, cmd) =>
      actorSelection(id).resolveOne(1 second).onComplete {
        case Success(room) =>
          room ! cmd
        case Failure(_) =>
          log.warning("Room not found: {}", id)
      }

    case x ⇒
      log.warning("unhandled: {}", x)
  }

  override def postStop {
  }
}
