package de.pokerno.playground

import de.pokerno.backend.gateway.{Http, http}
import de.pokerno.format.text
import akka.actor.{ActorSystem, Props}

object Main {
  val system = ActorSystem("poker-playground")
  
  def main(args: Array[String]) = {
    val htmlEventSource = system.actorOf(Props(classOf[Http.Gateway],
        http.Config(port = 8080, eventSource = Right(true))))
    
    
  }
}
