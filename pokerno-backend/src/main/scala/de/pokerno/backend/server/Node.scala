package de.pokerno.backend.server

import org.slf4j.{Logger, LoggerFactory}

import de.pokerno.backend.{gateway => gw}
import de.pokerno.protocol.rpc

import akka.actor.{Actor, Props, ActorSystem}

class Node extends Actor {
  override def preStart {
    
  }
  
  def receive = {
    case _ =>
  }
  
  override def postStop {
    
  }
}

object Node {
  val log = LoggerFactory.getLogger(getClass)
  lazy val system = ActorSystem("node")

  def start(config: Config) {
    log.info("starting node with config: {}", config)
    
    config.rpc.map { rpc =>
    }

    config.zeromq.map { zmq =>
      log.info("starting zmq gateway with config: {}", zmq)
      val zmqGateway = system.actorOf(Props(classOf[gw.Zeromq], zmq), name = "zeromq")
    }

    config.http.map { httpConfig =>
      log.info("starting HTTP gateway")
      val httpGateway = system.actorOf(Props(classOf[gw.Http.Gateway]), name = "http-gateway")
      
      def startHttpServer = {
        log.info("starting HTTP server with config: {}", httpConfig)
        val server = new gw.http.Server(httpGateway, httpConfig)
        server.start
      }
      startHttpServer
    }

  }
}
