package de.pokerno.backend.server

import org.slf4j.{ Logger, LoggerFactory }
import akka.actor.{ Actor, ActorLogging, ActorRef, Props, ActorSystem }

import de.pokerno.model.{Variation, Stake}
import de.pokerno.form.Room.{Topics => RoomTopics}
import de.pokerno.backend.{ gateway ⇒ gw }
import de.pokerno.backend.Gateway
import de.pokerno.backend.node.Setup
import de.pokerno.protocol._
import de.pokerno.client.payment
import de.pokerno.client.sync

object Node {
  val log = LoggerFactory.getLogger(getClass)

  implicit val system = ActorSystem("node")

  def start(config: Config, restoreFile: Option[String]): ActorRef = {
    val id = config.id
    log.info(f"starting node $id (${config.host})")

    val balance = payment.Client.buildClient(config.paymentAddress)
    val syncClient = new sync.Client(config.syncUrl)
    val node = system.actorOf(Props(classOf[Node], id, balance, syncClient, restoreFile), name = "node-main")

    val setup = new Setup(node)
    config.rpcAddress.map { addr ⇒
      setup.withRpc(addr)
    }

    config.http.map { c ⇒
      setup.withHttp(c, config.authService)
    }

    config.apiAddress.map { addr =>
      setup.withApi(addr)
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

  case class Metrics(
    id: String,
    metrics: de.pokerno.backend.node.Metrics
    )

}

class Node(
    val nodeId: java.util.UUID,
    val balance: payment.Client,
    val syncClient: sync.Client,
    restoreFile: Option[String] = None
    ) extends Actor with ActorLogging with de.pokerno.backend.node.Consumers {

  import context._
  import concurrent.duration._
  import util.{ Success, Failure }
  import CommandConversions._

  override def preStart {
    // setup metrics
    system.scheduler.schedule(1.minute, 1.minute) {
      metrics.report()
    }
    restoreFile.map(restore(_))
    fetch()
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
              balance,
              notificationConsumers,
              topicConsumers.toMap
            ), name = id)
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

  override def postStop {
  }

  import de.pokerno.protocol.Codec.{Json => codec}
  import collection.JavaConversions._
  def fetch() {
    val stream = syncClient.getRooms(nodeId.toString())
    val msgs = codec.decodeValuesFromStream[Node.CreateRoom](stream)
    msgs.foreach { self ! _ }
  }

  def restore(restorePath: String) {
    val stream = new java.io.FileInputStream(restorePath)
    val msgs = codec.decodeValuesFromStream[Node.CreateRoom](stream)
    msgs.foreach { self ! _ }
  }
}
