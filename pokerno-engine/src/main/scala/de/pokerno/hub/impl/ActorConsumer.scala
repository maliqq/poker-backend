package de.pokerno.hub.impl

import akka.actor.ActorRef

class ActorConsumer[T](ref: ActorRef) extends de.pokerno.hub.Consumer[T] {
  def consume(msg: T) {
    ref ! msg
  }
}
