package de.pokerno.backend.server

import akka.actor.{ Actor, Props }

import de.pokerno.gameplay

trait Observers {

  a: Actor ⇒

  def events: gameplay.Events
  def roomId: String

  import context._

  def observe[T <: Actor](actorClass: Class[T], name: String, args: Any*) = {
    val actor = actorOf(Props(actorClass, args: _*), name = name)
    events.broker.subscribe(actor, name)
    actor
  }

}
