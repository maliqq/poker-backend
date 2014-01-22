package de.pokerno.backend.gateway

import akka.actor.{ ActorSystem, Props }

object Main {
  val system = ActorSystem("http-gateway")
  
  def main(args: Array[String]) = {
    val gw = system.actorOf(Props(classOf[Http.Gateway],
        http.Config(port = 8080, webSocket = Right(true), eventSource = Right(true))))
  }
}
