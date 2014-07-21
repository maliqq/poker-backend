package de.pokerno.form.tournament

import akka.actor.{ActorSystem, Props}

object Main {
  
  def main(args: Array[String]) {
    
    val system = ActorSystem("tournament")
    val t = system.actorOf(Props(classOf[Tournament]))
    
  }

}
