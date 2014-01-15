package de.pokerno.backend.gateway

import akka.actor.{Actor, ActorSystem, Props}
import io.netty.channel.Channel
import de.pokerno.backend.server.hub

import de.pokerno.protocol.{msg => message, Codec => codec}

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
          system.scheduler.schedule(0 milliseconds, 1 second, self, Tick(conn))
        }
      
      case Tick(conn) =>
        Console printf("tick!\n")
        conn.write("hello!")
      
      case http.Gateway.Disconnect(channel) =>
        val conn = channelConnections.remove(channel)
        conn.map { conn =>
          Console printf("disconnected!\n")
        }
      
      case http.Gateway.Message(channel, msg) =>
        Console printf("got: %s", msg)
      
      case msg: message.Message =>
        broadcast(codec.Json.encode(msg))
    }
    
    def broadcast(msg: Any) = channelConnections.foreach { case (channel, conn) =>
      conn.write(msg)
    }
    
    override def postStop {
      server.stop
    }
  }
}
