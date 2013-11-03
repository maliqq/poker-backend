package de.pokerno.backend.gateway

import org.webbitserver._

object EventSource {
  class Server extends Runnable {
    final val port = 8082
    final val path = "/_events"

    def run {
      val instance = WebServers.createWebServer(port).
        add(path, new EventSource()).
        start().
        get
    }
  }
}

class EventSource extends EventSourceHandler {
  def onOpen(conn: EventSourceConnection) {
    
  }
  
  def onClose(conn: EventSourceConnection) {
    
  }
}
