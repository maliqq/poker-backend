package de.pokerno.backend.server

import de.pokerno.backend.engine._
import de.pokerno.backend.model._

import akka.actor.{ ActorSystem, Props }

object Main {
  val system = ActorSystem("poker-server")

  def main(args: Array[String]) {
    val instance = system actorOf (Props[Instance], name = "greeter")
    instance ! Instance.Start
  }
}
