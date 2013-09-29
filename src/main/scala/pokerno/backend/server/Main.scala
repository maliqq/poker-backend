package pokerno.backend.server

import pokerno.backend.engine._
import akka.actor.{ActorSystem, Props}

object Main {
  def main(args: Array[String]) {
    val system = ActorSystem("MySystem")
    val instance = system.actorOf(Props[Instance], name = "greeter")
    instance ! Instance.Start
  }
}
