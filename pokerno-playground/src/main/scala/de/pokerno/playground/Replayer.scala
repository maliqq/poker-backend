package de.pokerno.playground

import de.pokerno.format.text.Lexer.Tags
import akka.actor.Actor

class Replayer extends Actor {
  override def receive = {
    case Tags.AllIn(player) =>
    case _ =>
  } 
}
