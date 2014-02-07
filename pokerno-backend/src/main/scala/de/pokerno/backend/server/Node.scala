package de.pokerno.backend.server

import org.slf4j.{ Logger, LoggerFactory }

import de.pokerno.backend.{ gateway ⇒ gw }
import de.pokerno.protocol.rpc
import de.pokerno.backend.Connection

import akka.actor.{ Actor, ActorRef, Props, ActorSystem }

class Node extends Actor {
  override def preStart {

  }

  val rooms = collection.mutable.HashMap[String, ActorRef]()

  def receive = {
    case Room.Start(id: String) ⇒
      if (!rooms.contains(id)) {
        val room = context.actorOf(Props(classOf[Room]), name = f"room-$id")
        rooms += (id -> room)
      }

    case Room.Stop(id: String) ⇒
      rooms.remove(id).map { room ⇒
        context.stop(room)
      }

    case Room.Send(id: String, msg: Any) ⇒
      rooms.get(id).map { room ⇒
        room ! msg
      }

    case Node.Status(conn) ⇒
      val resp = new java.util.HashMap[String, Any]()
      resp.put("roomsCount", rooms.size)
      conn.send(resp)

    case _ ⇒
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
