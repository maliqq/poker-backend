package main.scala.pokerno.backend.server

import com.twitter.util.Future
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.Service
import com.twitter.finagle.http.Http
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.websocket._
import java.net.InetSocketAddress
import akka.actor.{Actor, ActorSystem, Props}

class Instance extends Actor {
  object State {
    case object Stop
    case object Start
  }
  
  def receive = {
    case State.Stop => context.stop(self)
  }
}

class NodeService extends Service[HttpRequest, HttpResponse] {
  def apply(request: HttpRequest) = Future.value(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
}

object Node {
  val service: Service[HttpRequest, HttpResponse] = new NodeService()
  
  val address: InetSocketAddress = new InetSocketAddress(8080)
  
  def main(args: Array[String]) {
    val server: Server = ServerBuilder().codec(Http()).bindTo(address).name("node_service").build(service)
  }
}

class RoomService extends Service[HttpRequest, HttpResponse] {
  def apply(request: HttpRequest) = Future.value(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
}

object Room {
  
}
