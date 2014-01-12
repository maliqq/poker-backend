package de.pokerno.backend.gateway

import akka.actor.{Actor, ActorSystem, Props}

object Http {
  class Gateway(config: http.Config) extends Actor {

    import concurrent.duration._
    import context._
    
    lazy val server = http.Server(config, self)
    
    override def preStart {
      server.start
    }
    
    case class Tick(conn: http.Connection)
    
    def receive = {
      case http.Gateway.Connect(conn) =>
        Console printf("%s connected!\n", conn.remoteAddr)
        system.scheduler.schedule(0 milliseconds, 1 second, self, Tick(conn))
      
      case Tick(conn) =>
        Console printf("tick!\n")
        conn.write("hello!")
      
      case http.Gateway.Disconnect(conn) =>
        Console printf("disconnected!\n")
      
      case http.Gateway.Message(conn, msg) =>
        Console printf("got: %s", msg)
    }
    
    override def postStop {
      server.stop
    }
  }
}
