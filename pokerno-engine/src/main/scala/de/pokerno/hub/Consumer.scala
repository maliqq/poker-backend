package de.pokerno.hub

trait Consumer {
  def consume(msg: Message)
}

import akka.actor.ActorRef
sealed class ActorConsumer(ref: ActorRef) extends Consumer {
  def consume(msg: Message) {
    ref ! msg
  }
}
