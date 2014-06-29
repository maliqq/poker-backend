package de.pokerno.model.table.seat

import concurrent.duration._
import de.pokerno.model.Countdown

trait Timers {
  
  val actingTimer       = new Countdown("acting", 30.seconds)
  val reconnectTimer    = new Countdown("reconnect", 15.seconds)
  
}
