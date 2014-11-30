package de.pokerno.backend.server

import org.slf4j.{ Logger, LoggerFactory }
import akka.actor.{ Actor, ActorLogging, ActorRef, Props, ActorSystem }

import de.pokerno.model.{Variation, Stake}
import de.pokerno.form.Room.{Topics => RoomTopics}
import de.pokerno.backend.{ gateway ⇒ gw }
import de.pokerno.backend.Gateway
import de.pokerno.backend.server.node.Bootstrap
import de.pokerno.protocol._
import de.pokerno.data.pokerdb
import de.pokerno.data.db

object Node {
  val log = LoggerFactory.getLogger(getClass)

  implicit val system = ActorSystem("node")
  
  def start(config: Config): ActorRef = {
    val id = config.id
    log.info(f"starting node $id (${config.host})")
    
    val connector = config.loadedDbProps.map { props =>
      db.Connection.connector(props)
    }
    val broadcasts = collection.mutable.ListBuffer[Tuple2[Broadcast, List[String]]]()
    config.broadcast.map { bcast =>
      bcast.redis.map { addr =>
        broadcasts += Tuple2(Broadcast.Redis(addr), List(RoomTopics.State, RoomTopics.Metrics))
      }
    }
    
    val node = system.actorOf(Props(classOf[Node], id, broadcasts.toList, connector), name = "node-main")
    
    val boot = new Bootstrap(node)
    config.rpc.map { c ⇒
      boot.withRpc(c.addr)
    }
    
    config.http.map { c ⇒
      boot.withHttp(c, config.authService)
    }
    
    config.apiAddress.map { addr =>
      boot.withApi(addr)
    }

    node
  }

  import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnoreProperties}
  
  @JsonIgnoreProperties(ignoreUnknown = true)
  case class CreateRoom(
    @JsonProperty id: String,
    @JsonProperty variation: Variation,
    @JsonProperty stake: Stake
  )
  
  case class ChangeRoomState(
    id: String,
    newState: de.pokerno.form.Room.ChangeState
  )
  
  case class SendCommand(
    id: String,
    command: cmd.Command
  )
  
  case object Metrics
  
}

class Node(
    val nodeId: java.util.UUID,
    val broadcastTopics: List[Tuple2[Broadcast, List[String]]] = List(),
    val sessionConnector: Option[()=>org.squeryl.Session] 
  ) extends Actor with ActorLogging with node.Initialize {
  
  import context._
  import concurrent.duration._
  import util.{ Success, Failure }
  import CommandConversions._
  
  val balance = new de.pokerno.payment.Service()
  
  override def preStart {
    system.scheduler.schedule(1.minute, 1.minute) {
      metrics.report()
    }
  }

  def receive = {
    // new connection
    case msg @ Gateway.Connect(conn) if conn.room.isDefined ⇒
      metrics.connected(conn.player.isDefined)
      
      val id = conn.room.get

      tryFindActor(id)  {
        case Success(room) ⇒
          room ! de.pokerno.form.Room.Connect(conn)

        case Failure(_) ⇒
          log.warning("room {} not found for conn {}", id, conn)
          conn.close()
      }

    case msg @ Gateway.Disconnect(conn) if conn.room.isDefined ⇒
      metrics.disconnected(conn.player.isDefined)
      
      val id = conn.room.get

      tryFindActor(id) {
        case Success(room) ⇒
          room ! de.pokerno.form.Room.Disconnect(conn)

        case Failure(_) ⇒
          log.warning("room {} not found for conn {}", id, conn)
      }

    // catch player messages
    case msg @ Gateway.Message(conn, event) if conn.player.isDefined && conn.room.isDefined ⇒
      metrics.messageReceived()
      
      implicit val player = conn.player.get
      val id = conn.room.get

      findRoom(id) { room =>
        room ! (event: Command)
      }

    case Node.CreateRoom(id, variation, stake) ⇒
      actorSelection(id).resolveOne(1 second).onComplete {
        case Failure(_) ⇒
          log.info("spawning new room with id={}", id)
          val room = context.actorOf(Props(classOf[Room], java.util.UUID.fromString(id), // FIXME
              variation,
              stake,
              newRoomEnv), name = id)
          room
        case _ ⇒
          log.warning("Room exists: {}", id)
      }

    case Node.ChangeRoomState(id, newState) ⇒ findRoom(id) { room =>
      room ! newState
    }
    
    case Node.SendCommand(id, cmd) => findRoom(id) { room =>
      room ! cmd
    }
      
    case Node.Metrics =>
      sender ! metrics.registry.getMetrics()

    case x ⇒
      log.warning("unhandled: {}", x)
  }
  
  private def findRoom(id: String)(f: (ActorRef) => Any) {
    tryFindActor(id) {
      case Success(room) =>
        f(room)
      case Failure(_) =>
        log.warning("Room not found: {}", id)
    }
  }
  
  private def tryFindActor(id: String)(f: util.Try[ActorRef] => Any) {
    actorSelection(id).resolveOne(1 second).onComplete(f)
  }
  
  private def newRoomEnv = {
    RoomEnv(balance,
      notificationConsumers = notificationConsumers,
      topicConsumers = topicConsumers.toMap)
  }

  override def postStop {
  }
}
