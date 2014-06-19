package de.pokerno.backend.server

import akka.actor.{ Actor, ActorRef, Props }

import de.pokerno.gameplay

trait Observers { a: Actor ⇒

  def events: gameplay.Events
  def roomId: String

  import context._

  def observe[T <: Actor](actorClass: Class[T], name: String, args: Any*) = {
    val ref = actorOf(Props(actorClass, args: _*), name = name)
    notify(ref, name)
    ref
  }
  
  def notify(ref: ActorRef, name: String) = {
    events.broker.subscribe(ref, name)
  }

}
