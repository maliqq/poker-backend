package de.pokerno.backend.gateway

import org.{ webbitserver => webbit }
import akka.actor.{ Actor, ActorLogging, ActorRef }

object Websocket {

  final val DefaultPort = 8080
  final val DefaultPath = "/_ws"

  case class Config(
      val port: Int = DefaultPort,
      val path: String = DefaultPath
  )
  
  case class Connect(conn: webbit.WebSocketConnection)
  case class Disconnect(conn: webbit.WebSocketConnection)
  
  class Server(config: Config) extends Actor with ActorLogging {
    val port = config.port
    val path = config.path
    
    override def preStart {
      log info("starting %s at %s".format(path, port))
      
      val instance = webbit.WebServers.createWebServer(port).
        add(path, new Handler(self)).
        start.
        get
    }
    
    def receive = {
      case Connect(conn) =>
      case Disconnect(conn) =>
      case _ =>
    }
  }

  class Handler(server: ActorRef) extends webbit.BaseWebSocketHandler {
    override def onOpen(conn: webbit.WebSocketConnection) {
      server ! Connect(conn)
    }
  
    override def onClose(conn: webbit.WebSocketConnection) {
      server ! Disconnect(conn)
    }
  
    override def onMessage(conn: webbit.WebSocketConnection, message: String) {
      Console printf("Got %s\n", message)
      conn.send(message)
    }
  }
}
