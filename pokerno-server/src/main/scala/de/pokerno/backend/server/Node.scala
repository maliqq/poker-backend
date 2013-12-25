package de.pokerno.backend.server

import com.twitter.util.Future
import com.twitter.finagle
import com.twitter.finagle.http.Http
import org.jboss.netty.handler.codec.http._
import java.net.InetSocketAddress
import java.util.UUID

import akka.actor.{ActorSystem, Props}

import de.pokerno.model._
import de.pokerno.backend.{gateway => gw}

object Node {
  class Service extends finagle.Service[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest) = {
      Future value (new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
    }

    // create new room
    case class createRoomRq(guid: UUID, variation: Variation, stake: Stake)
    def createRoom(r: createRoomRq) = {}

    // close existing room
    case class closeRoomRq(guid: UUID)
    def closeRoom(r: closeRoomRq) = {}
  }
}

class Node(val config: Config) extends Runnable {
  def run {
    start
  }
  
  val actorSystem = ActorSystem("poker-node")

  import com.twitter.finagle.builder.{ Server => RpcServer, ServerBuilder => RpcServerBuilder }

  def start {
    config.rpc.map { rpc =>
      val address = new InetSocketAddress(rpc.host, rpc.port)
      val rpcService = RpcServerBuilder().codec(Http()).bindTo(address).
        name("node-rpc-service").
        build(new Node.Service)
    }
    
    config.zeromq.map { zmq =>
      val zmq = actorSystem.actorOf(Props(classOf[gw.Zeromq]), name = "zeromq")
    }
    
    config.http.map { http =>
      http.api.map { api =>
        
      }
      
      http.eventSource.map { es =>
        val eventSource = actorSystem.actorOf(Props(classOf[gw.EventSource.Server], es), name = "eventSource")
      }
      
      http.webSocket.map { ws =>
        val webSocket = actorSystem.actorOf(Props(classOf[gw.Websocket.Server], ws), name = "webSocket")
      }
    }
  }
}
