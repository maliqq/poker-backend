package de.pokerno.gameplay.stg

import akka.actor.ActorRef
import de.pokerno.protocol.GameEvent
import de.pokerno.gameplay.{ Context => Gameplay}

class Context(
    val gameplay: Gameplay,
    val ref: ActorRef
  ) {
  
  def publish(e: GameEvent)     = gameplay.events.publish(e)_
  def broadcast(e: GameEvent)   = gameplay.events.broadcast(e)
}
