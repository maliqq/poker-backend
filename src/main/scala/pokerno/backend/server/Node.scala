package pokerno.backend.server

import com.twitter.util.Future
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle
import com.twitter.finagle.http.Http
import org.jboss.netty.handler.codec.http._
import java.net.InetSocketAddress

object Node {
  class Service extends finagle.Service[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest) = {
      Future.value(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
    }
  }
  
  val service: finagle.Service[HttpRequest, HttpResponse] = new Service
  
  object Config {
    var name = "node_service"
    var host: String = "localhost"
    var port: Int = 8081
  }
}

class Node extends Runnable {
  def run {
    val address = new InetSocketAddress(Node.Config.host, Node.Config.port)
    val server: Server = ServerBuilder().codec(Http()).bindTo(address).name(Node.Config.name).build(Node.service)
  }
}
