package de.pokerno.backend.gateway

import org.{ webbitserver => webbit }
import de.pokerno.backend.protocol
import akka.actor.{ Actor, ActorLogging, ActorRef }
import scala.concurrent.duration._

object EventSource {
  case class Connect(conn: webbit.EventSourceConnection)
  case class Disconnect(conn: webbit.EventSourceConnection)
  
  case object Ping
  
  final val DefaultPort = 8082
  final val DefaultPath = "/_events"
  
  case class Config(
      val port: Int = DefaultPort,
      val path: String = DefaultPath
      )
  
  class Server(config: Config) extends Actor with ActorLogging {
    val port = config.port
    val path = config.path
    
    import context._
    var connections = new java.util.ArrayList[webbit.EventSourceConnection]()
  
    override def preStart {
      log info("starting %s at :%d\n".format(path, port))
      // FIXME
      val instance = webbit.WebServers.createWebServer(port).
        add(new CrossOriginHandler).
        add(path, new Handler(self)).
        start().
        get
      
      //system.scheduler.schedule(Duration.Zero, 5 seconds, self, Ping)
    }
    
    implicit def msg2EventSourceMessage(msg: protocol.Message): webbit.EventSourceMessage = {
      try {
        val data = protocol.Codec.Json.encodeExplicit(msg)
        new webbit.EventSourceMessage(new String(data.map(_.toChar)))
      } catch {
        case e: Exception =>
          new webbit.EventSourceMessage("error handling %s: %s %s".format(msg, e, e.getStackTraceString))
      }
    }
    
    def receive = {
      case Ping =>
        broadcast(new webbit.EventSourceMessage("ping"))

      case Connect(conn) =>
        log debug("connect %s".format(conn.httpRequest.remoteAddress))
        
        connections.add(conn)
      
      case Disconnect(conn) =>
        log debug("disconnect %s".format(conn.httpRequest.remoteAddress))
        
        connections.remove(conn)
        
      case msg: protocol.Message =>
        broadcast(msg)
    }
    
    def broadcast(msg: webbit.EventSourceMessage) {
      log debug("broadcast %s".format(msg))
      
      val iter = connections.iterator
      while (iter.hasNext){
        val conn = iter.next
        conn.send(msg)
      }
    }
  }
  
  class Handler(server: ActorRef) extends webbit.EventSourceHandler {
    def onOpen(conn: webbit.EventSourceConnection) {
      server ! Connect(conn)
    }
    
    def onClose(conn: webbit.EventSourceConnection) {
      server ! Disconnect(conn)
    }
  }

}

class CrossOriginHandler extends webbit.HttpHandler {
  import org.jboss.netty.handler.codec.http
  
  def handleHttpRequest(req: webbit.HttpRequest, resp: webbit.HttpResponse, control: webbit.HttpControl) {
    resp.header(http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
    resp.header(http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS, "*")
    
    control.nextHandler
  }
}
