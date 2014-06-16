package de.pokerno.model.seat

import akka.actor.Cancellable

import de.pokerno.model.Countdown

trait Timers {
  
  val actingTimer = new Countdown("acting", 10)
  val discardingTimer = new Countdown("discarding", 10)
  val reconnectTimer = new Countdown("reconnect", 10)
  
}
