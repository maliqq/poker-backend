package pokerno.backend.server

import org.webbitserver.{ WebServers, BaseWebSocketHandler, WebSocketConnection }

object WebsocketServer {

  object Config {
    val port = 8080
    val path = "/_ws"
  }

}

class WebsocketHandler extends BaseWebSocketHandler {
  override def onOpen(conn: WebSocketConnection) {
  }

  override def onClose(conn: WebSocketConnection) {
  }

  override def onMessage(conn: WebSocketConnection, message: String) {
  }
}

class WebsocketServer extends Runnable {

  def run {
    val server = WebServers.createWebServer(WebsocketServer.Config.port).
      add(WebsocketServer.Config.path, new WebsocketHandler).
      start.
      get
  }

}
