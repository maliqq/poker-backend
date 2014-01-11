package de.pokerno.backend.gateway

import akka.actor.{ ActorSystem, Props }

object Main {
  val system = ActorSystem("test-gateway")
  
  def main_(args: Array[String]) {
    val ref = system.actorOf(Props(classOf[EventSource.Server]))
  }
}
