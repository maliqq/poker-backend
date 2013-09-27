package pokerno.backend.server

import com.twitter.util.Future
import com.twitter.finagle
import com.twitter.finagle.http.Http
import org.jboss.netty.handler.codec.http._

object Room {
  class Service extends finagle.Service[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest) = {
      Future.value(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
    }
  }
}

class Room {

}
