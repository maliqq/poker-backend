package de.pokerno.backend.server

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
  lazy val system = ActorSystem("node")

  def start(config: Config) {
    config.rpc.map { rpc =>
    }

    config.zeromq.map { zmq =>
      val zmqGateway = system.actorOf(Props(classOf[gw.Zeromq]), name = "zeromq")
    }

    config.http.map { http =>
      val httpGateway = system.actorOf(Props(classOf[gw.Http.Gateway], http), name = "http")
    }

  }
}
