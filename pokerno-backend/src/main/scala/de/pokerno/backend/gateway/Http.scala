package de.pokerno.backend.gateway

import akka.actor.Actor
import io.netty.channel.Channel
import io.netty.buffer.Unpooled
import io.netty.util.CharsetUtil

import de.pokerno.protocol.{Message, Codec => codec}

object Http {
  class Dispatcher extends Actor {

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
          Console printf("%s connected!\n", conn.remoteAddr)
          //system.scheduler.schedule(0 milliseconds, 1 second, self, Tick(conn))
        }
      
//      case Tick(conn) =>
//        Console printf("tick!\n")
//        conn.write("hello!")
//      
      case http.Event.Disconnect(channel) =>
        val conn = channelConnections.remove(channel)
        conn.map { conn =>
          Console printf("disconnected!\n")
        }
      
      case http.Event.Message(channel, msg) =>
        Console printf("got: %s", msg)
      
      case msg: Message =>
        broadcast(codec.Json.encode(msg))
      
      case _ =>
    }
    
    def broadcast(msg: Any) = channelConnections.foreach { case (channel, conn) =>
      conn.send(msg)
    }
    
    override def postStop {
    }
  }
}
