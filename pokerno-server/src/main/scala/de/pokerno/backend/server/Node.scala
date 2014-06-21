package de.pokerno.backend.server

import org.slf4j.{ Logger, LoggerFactory }
import de.pokerno.model.{Variation, Stake}
import de.pokerno.model.Seat.{State => SeatState}
import de.pokerno.backend.{ gateway ⇒ gw }
import de.pokerno.backend.Gateway
import de.pokerno.backend.gateway.http
import de.pokerno.protocol._
import akka.actor.{ Actor, ActorLogging, ActorRef, Props, ActorSystem }
import de.pokerno.backend.server.node.Service

object Node {

  val log = LoggerFactory.getLogger(getClass)
  implicit val system = ActorSystem("node")
  
  def start(config: Config): ActorRef = {
    log.info("starting with config: {}", config)
    
    val session = config.dbProps.map { file =>
      val f = new java.io.FileInputStream(file)
      val props = new java.util.Properties
      props.load(f)
      //de.pokerno.db.Connection.connect(props)
    }
    
    log.info("starting node at {}", config.host)
    val node = system.actorOf(Props(classOf[Node], session), name = "node-main")

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

class Node(session: Option[org.squeryl.Session]) extends Actor with ActorLogging {
  import context._
  import concurrent.duration._
  import util.{ Success, Failure }
  import CommandConversions._

  val balance = new de.pokerno.finance.Service()
  val persist = actorOf(Props(classOf[Persistence], session),
      name = "node-persist")
  val metrics = actorOf(Props(classOf[node.Metrics]),
      name = "node-metrics")
  val history = actorOf(Props(classOf[PlayHistoryWriter]))
  val broadcasts = Seq[Broadcast](
      //new Broadcast.Zeromq("tcp://127.0.0.1:5555")
  )
  
  override def preStart {
  }

  def receive = {
    // new connection
    case msg @ Gateway.Connect(conn) if conn.room.isDefined ⇒
      metrics ! msg
      
      val id = conn.room.get

      actorSelection(id).resolveOne(1 second).onComplete {
        case Success(room) ⇒
          room ! Room.Connect(conn)

        case Failure(_) ⇒
          log.warning("room {} not found for conn {}", id, conn)
          conn.close()
      }

    case msg @ Gateway.Disconnect(conn) if conn.room.isDefined ⇒
      metrics ! msg
      
      val id = conn.room.get

      actorSelection(id).resolveOne(1 second).onComplete {
        case Success(room) ⇒
          room ! Room.Disconnect(conn)

        case Failure(_) ⇒
          log.warning("room {} not found for conn {}", id, conn)
      }

    // catch player messages
    case msg @ Gateway.Message(conn, event) if conn.player.isDefined && conn.room.isDefined ⇒
      metrics ! msg
      
      implicit val player = conn.player.get
      val id = conn.room.get

      actorSelection(id).resolveOne(1 second).onComplete {
        case Success(room) ⇒
          room ! (event: Command)

        case Failure(_) ⇒
          log.warning("Room not found: {}", id)
      }

    case Node.CreateRoom(id, variation, stake) ⇒
      actorSelection(id).resolveOne(1 second).onComplete {
        case Failure(_) ⇒
          log.info("spawning new room with id={}", id)
          val room = context.actorOf(Props(classOf[Room],
              java.util.UUID.fromString(id), // FIXME
              variation,
              stake,
              balance, persist, history, broadcasts), name = id)
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
