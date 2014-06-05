package de.pokerno.replay

import de.pokerno.model.Street
import de.pokerno.gameplay.stg
import de.pokerno.gameplay.{Context => Gameplay}

import akka.actor.ActorRef

import concurrent.duration._
import concurrent.duration.Duration

private[replay] class Context(
    _gameplay: Gameplay,
    _ref: ActorRef
) extends stg.Context(_gameplay, _ref) {
  
  import gameplay._
  
  def sleep() = Thread.sleep(speed.toMillis)
  
  private val _streets = Street.byGameGroup(gameOptions.group)
  
  var speed: Duration = null
  var bettingStarted: Boolean = false
  
  var streets = _streets
}
