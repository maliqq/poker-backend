package de.pokerno.backend.server

import de.pokerno.backend._
import de.pokerno.model._
import akka.actor.{ ActorSystem, Props }
import de.pokerno.backend.Instance

object Main {
  val system = ActorSystem("poker-server")

  def main(args: Array[String]) {
    val instance = system actorOf (Props[Instance], name = "greeter")
    instance ! Instance.Start
  }
}
