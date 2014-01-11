package de.pokerno.backend.gateway

import akka.actor.Actor

object Http {
  final val defaultPort = 8082
  
  object EventSource {
    final val defaultPath = "/_events"
    
    case class Config(val path: String)
  }
  
  object WebSocket {
    final val defaultPath = "/_ws"
    
    case class Config(val path: String)
  }
  
  case class Config(
      port: Int,
      eventSource: EventSource.Config,
      webSocket: WebSocket.Config
    )
    
  class Gateway extends Actor {
    def receive = {
      case _ =>
    }
  }
}
