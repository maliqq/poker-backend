package de.pokerno.backend.server

import org.slf4j.{ Logger, LoggerFactory }

import de.pokerno.model
import de.pokerno.backend.{ gateway ⇒ gw }
import de.pokerno.backend.{ rpc => zerorpc }
import de.pokerno.protocol.{rpc, cmd}
import de.pokerno.protocol.{msg => message}
import de.pokerno.protocol.rpc.Conversions._
import de.pokerno.backend.Connection

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, ActorSystem }

class Node extends Actor with ActorLogging {
  import context._
  import concurrent.duration._
  import util.{Success, Failure}
  
  override def preStart {
  }

  def receive = {
    // catch player messages
    case (player: String, id: String, msg: message.Inbound) =>
      system.actorSelection(id).resolveOne(1 second).onComplete {
        case Success(room) =>
          msg match {
            case join: message.JoinTable => 
              room ! cmd.JoinPlayer(join.pos, player, join.amount)

            case add: message.AddBet =>
              room ! cmd.AddBet(player, add.bet)
          }
        
        case Failure(_) =>
          log.warning("Room not found: {}", id)
      }

    case create: rpc.CreateRoom ⇒
      val id = create.id
      system.actorSelection(id).resolveOne(1 second).onComplete {
        case Failure(_) =>
          spawnRoom(create)
        case _ =>
          log.warning("Room exists: {}", id)
      }

    case action: rpc.RoomAction ⇒
      import rpc.RoomActionSchema._
      val id = action.id
      system.actorSelection(id).resolveOne(1 second).onComplete {
        case Success(room) =>
          action.`type` match {
            case ActionType.PAUSE => room ! Room.Pause
            case ActionType.CLOSE => room ! Room.Close
            case ActionType.RESUME => room ! Room.Resume
            case _ => // TODO
          }
        case Failure(_) =>
          log.warning("Room not found: {}", id)
      }
    
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

  val log = LoggerFactory.getLogger(getClass)
  lazy val system = ActorSystem("node")

  def start(config: Config): ActorRef = {
    log.info("starting with config: {}", config)

    log.info("starting node {}", config.host)
    val node = system.actorOf(Props(classOf[Node]), name = f"node-${config.host}")
    
    config.rpc.map { rpcConfig ⇒
      log.info("starting zmq rpc with config: {}", rpcConfig)
      val zmqRpc = system.actorOf(Props(classOf[zerorpc.Zeromq], node))
    }
    
    config.zeromq.map { zmqConfig ⇒
      log.info("starting zmq gateway with config: {}", zmqConfig)
      val zmqGateway = system.actorOf(Props(classOf[gw.Zeromq], zmqConfig), name = "zeromq")
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
    
    node
  }

}
