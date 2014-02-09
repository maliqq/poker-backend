package de.pokerno.backend.server

import org.slf4j.{ Logger, LoggerFactory }

import de.pokerno.model
import de.pokerno.backend.{ gateway ⇒ gw }
import de.pokerno.protocol.rpc
import de.pokerno.protocol.rpc.Conversions._
import de.pokerno.backend.Connection

import akka.actor.{ Actor, ActorRef, Props, ActorSystem }

class Node extends Actor {
  import context._
  import concurrent.duration._
  import util.{Success, Failure}
  
  override def preStart {
  }

  def receive = {
    case create: rpc.CreateRoom ⇒
      system.actorSelection(create.id).resolveOne(1 second).onComplete {
        case Failure(_) =>
          spawnRoom(create)
        case _ =>
      }

    case action: rpc.RoomAction ⇒
      import rpc.RoomActionSchema._
      system.actorSelection(action.id).resolveOne(1 second).onComplete {
        case Success(room) =>
          action.`type` match {
            case ActionType.PAUSE => room ! Room.Pause
            case ActionType.CLOSE => room ! Room.Close
            case ActionType.RESUME => room ! Room.Resume
            case _ => // TODO
          }
        case _ =>
      }
    
    case Node.Status(conn) ⇒

    case _ ⇒
  }
  
  private def spawnRoom(createRequest: rpc.CreateRoom): ActorRef = {
    val id = createRequest.id
    val variation: model.Variation = createRequest.variation
    val stake: model.Stake = createRequest.stake
    
    context.actorOf(Props(classOf[Room], id, variation, stake), name = id)
  }

  override def postStop {
  }
}

object Node {

  case class Status(conn: Connection)

  val log = LoggerFactory.getLogger(getClass)
  lazy val system = ActorSystem("node")

  def start(config: Config) {
    log.info("starting with config: {}", config)

    config.rpc.map { rpc ⇒
    }

    config.zeromq.map { zmq ⇒
      log.info("starting zmq gateway with config: {}", zmq)
      val zmqGateway = system.actorOf(Props(classOf[gw.Zeromq], zmq), name = "zeromq")
    }

    config.http.map { httpConfig ⇒
      log.info("starting HTTP gateway")
      val httpGateway = system.actorOf(Props(classOf[gw.Http.Gateway]), name = "http-gateway")

      def startHttpServer = {
        log.info("starting HTTP server with config: {}", httpConfig)
        val server = new gw.http.Server(httpGateway, httpConfig)
        server.start
      }
      startHttpServer
    }

    log.info("starting node {}", config.host)
    system.actorOf(Props(classOf[Node]), name = f"node-${config.host}")

  }
}
