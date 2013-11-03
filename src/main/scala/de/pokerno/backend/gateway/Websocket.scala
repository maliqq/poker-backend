package de.pokerno.backend.gateway

import org.webbitserver.{ WebServers, BaseWebSocketHandler, WebSocketConnection }
import akka.actor.ActorSystem

object Websocket {

  final val DefaultWebsocketPort = 8080
  final val DefaultWebsocketPath = "/_ws"

  object Config {
    val port = DefaultWebsocketPort
    val path = DefaultWebsocketPath
  }

}

class WebsocketHandler(val system: ActorSystem) extends BaseWebSocketHandler {
  override def onOpen(conn: WebSocketConnection) {
    Console printf("Opened!\n")
  }

  override def onClose(conn: WebSocketConnection) {
    Console printf("Closed!\n")
  }

  override def onMessage(conn: WebSocketConnection, message: String) {
    Console printf("Got %s\n", message)
    conn.send(message)
  }
}

class Websocket(system: ActorSystem) extends Runnable {

  val port = Websocket.Config.port
  val path = Websocket.Config.path
  
  def run {
    val server = WebServers.createWebServer(port).
      add(path, new WebsocketHandler(system)).
      start.
      get
  }

}
