package de.pokerno.model.table.seat

import akka.actor.Cancellable

import de.pokerno.model.Countdown

trait Timers {
  
  val actingTimer       = new Countdown("acting", 30)
  val reconnectTimer    = new Countdown("reconnect", 15)
  
}
