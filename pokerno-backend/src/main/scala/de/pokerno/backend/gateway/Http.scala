package de.pokerno.backend.gateway

import concurrent.Promise
import akka.actor.{Actor, ActorSystem, Props}
import io.netty.channel.Channel
import io.netty.buffer.Unpooled
import io.netty.util.CharsetUtil
import de.pokerno.backend.server.hub

import de.pokerno.protocol.{Message, Codec => codec}

object Http {
  class Gateway(config: http.Config) extends Actor {

    import concurrent.duration._
    import context._
    
    lazy val server = http.Server(config, self)
    
    val channelConnections = new collection.mutable.HashMap[Channel, http.Connection]()
    
    override def preStart {
      server.start
    }
    
    case class Tick(conn: http.Connection)
    
    def receive = {
      case http.Gateway.Connect(channel, conn) =>
        if (!channelConnections.contains(channel)) {
          channelConnections.put(channel, conn)
          Console printf("%s connected!\n", conn.remoteAddr)
          //system.scheduler.schedule(0 milliseconds, 1 second, self, Tick(conn))
        }
      
//      case Tick(conn) =>
//        Console printf("tick!\n")
//        conn.write("hello!")
//      
      case http.Gateway.Disconnect(channel) =>
        val conn = channelConnections.remove(channel)
        conn.map { conn =>
          Console printf("disconnected!\n")
        }
      
      case http.Gateway.Message(channel, msg) =>
        Console printf("got: %s", msg)
      
      case msg: Message =>
        val data = codec.Json.encode(msg)
        val buf = Unpooled.copiedBuffer(data)
        val s = buf.toString(CharsetUtil.UTF_8)
        Console printf("%s--> SENDING %s%s\n", Console.CYAN, s, Console.RESET)
        broadcast(s)
      
      case _ =>
    }
    
    def broadcast(msg: Any) = channelConnections.foreach { case (channel, conn) =>
      conn.send(msg)
    }
    
    override def postStop {
      server.stop
    }
  }
}