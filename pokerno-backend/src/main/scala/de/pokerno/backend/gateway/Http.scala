package de.pokerno.backend.gateway

import akka.actor.{Actor, ActorLogging}
import io.netty.channel.Channel
import io.netty.buffer.Unpooled
import io.netty.util.CharsetUtil

import de.pokerno.protocol.{Message, Codec => codec}

object Http {
  class Gateway
    extends Actor with ActorLogging {

    import concurrent.duration._
    import context._

    val channelConnections = new collection.mutable.HashMap[Channel, http.Connection]()
    
    override def preStart {
    }
    
    case class Tick(conn: http.Connection)
    
    def receive = {
      case http.Event.Connect(channel, conn) =>
        if (!channelConnections.contains(channel)) {
          channelConnections.put(channel, conn)
          log.info("{} connected", conn)
        }

      case http.Event.Disconnect(channel) =>
        channelConnections.remove(channel).map { conn =>
          log.info("{} disconnected", conn)
        }
      
      case http.Event.Message(channel, data) =>
        //val msg = codec.Json.decode(data.getBytes)
      
      case msg: Message =>
        log.info("broadcasting {}", msg)
        broadcast(codec.Json.encode(msg))
      
      case x =>
        log.warning("unhandled: {}", x)
    }
    
    def broadcast(msg: Any) =
      channelConnections.foreach { case (channel, conn) =>
        conn.send(msg)
      }
    
    override def postStop {
    }
  }
}
