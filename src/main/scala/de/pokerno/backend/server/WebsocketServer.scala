package de.pokerno.backend.server

import org.webbitserver.{ WebServers, BaseWebSocketHandler, WebSocketConnection }
import akka.actor.ActorSystem

object WebsocketServer {

  final val DefaultWebsocketPort = 8080
  final val DefaultWebsocketPath = "/_ws"

  object Config {
    val port = DefaultWebsocketPort
    val path = DefaultWebsocketPath
  }

}

class WebsocketHandler(val system: ActorSystem) extends BaseWebSocketHandler {
  override def onOpen(conn: WebSocketConnection) {
  }

  override def onClose(conn: WebSocketConnection) {
  }

  override def onMessage(conn: WebSocketConnection, message: String) {
  }
}

class WebsocketServer(system: ActorSystem) extends Runnable {

  def run {
    val server = WebServers.createWebServer(WebsocketServer.Config.port).
      add(WebsocketServer.Config.path, new WebsocketHandler(system)).
      start.
      get
  }

}
