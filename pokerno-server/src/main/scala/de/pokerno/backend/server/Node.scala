package de.pokerno.backend.server

import org.slf4j.{ Logger, LoggerFactory }
import akka.actor.{ Actor, ActorLogging, ActorRef, Props, ActorSystem }

import de.pokerno.model.{Variation, Stake}
import de.pokerno.backend.{ gateway ⇒ gw }
import de.pokerno.backend.Gateway
import de.pokerno.backend.gateway.http
import de.pokerno.protocol._
import de.pokerno.backend.server.node.Bootstrap
import de.pokerno.data.pokerdb

trait Initialize {
  type Storage = de.pokerno.backend.storage.PostgreSQL.Storage
  
  def initDb(file: String)(implicit system: ActorSystem): NodeEnv = {
    val f = new java.io.FileInputStream(file)
    val props = new java.util.Properties
    props.load(f)
    
    system.actorOf(Props(new Actor with ActorLogging {
      val session = de.pokerno.data.db.Connection.connect(props)
      if (java.lang.Boolean.parseBoolean(props.getProperty("debug"))) {
        session.setLogger(println(_))
      }
      // FIXME
      org.squeryl.SessionFactory.externalTransactionManagementAdapter = Some(() => {
        Some(session)
      })
      
      override def preStart = {
        log.info("connection context started")
      }
      
      def receive = { case _ => }
    }))
    
    NodeEnv(
      Some(new Storage),
      Some(new pokerdb.Service())
    )
  }
}

case class NodeEnv(
    storage: Option[de.pokerno.backend.Storage] = None,
    db: Option[pokerdb.Service] = None
)

object Node extends Initialize {
  
  val log = LoggerFactory.getLogger(getClass)
  implicit val system = ActorSystem("node")
  
  def start(config: Config): ActorRef = {
    val id = config.id
    log.info(f"starting node $id (${config.host})")
    
    val env = config.dbProps.map { initDb(_) }.getOrElse(NodeEnv())
    
    val node = system.actorOf(Props(classOf[Node], id, env), name = "node-main")
    
    val boot = new Bootstrap(node)
    config.rpc.map { c ⇒
      boot.withRpc(c.addr)
    }
    
    val authService: Option[http.AuthService] = if (config.authEnabled && config.redis.isDefined) {
      val c = config.redis.get
      Some(new RedisAuthService(c.addr))
    } else None
    
    config.http.map { c ⇒
      boot.withHttp(c, authService)
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
    env: NodeEnv
  ) extends Actor with ActorLogging with node.Metrics {
  import context._
  import concurrent.duration._
  import util.{ Success, Failure }
  import CommandConversions._
  
  val pokerdb = env.db
  val balance = new de.pokerno.payment.Service()

  private val notificationConsumers = collection.mutable.ListBuffer[ActorRef]()
  ;{
    val persist = actorOf(Props(classOf[Persistence], pokerdb), name = "node-persist")
    notificationConsumers += persist
    env.storage.map { storage =>
      val history = actorOf(Props(classOf[de.pokerno.backend.PlayHistoryWriter], storage), name = "play-history-writer")
      notificationConsumers += history
    }
  }

  private val topicConsumers = collection.mutable.Map[String, List[ActorRef]]()
  ;{
    import de.pokerno.form.Room.Topics
    
    topicConsumers(Topics.State) = List(
      actorOf(Props(
        new Actor {
          val redis = Broadcast.Redis("127.0.0.1", 6379)
          def receive = {
            case _ =>
              redis.broadcast(Topics.State, "hello!")
          }
        }
      ))
    )
  }

  //val broadcasts = Seq[Broadcast](
      //new Broadcast.Zeromq("tcp://127.0.0.1:5555")
  //)
  
  override def preStart {
    startReporting()
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
    RoomEnv(balance, pokerdb,
      notificationConsumers = notificationConsumers,
      topicConsumers = topicConsumers.toMap)
  }

  override def postStop {
  }
}
