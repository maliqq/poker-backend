package de.pokerno.model.table.seat

import com.fasterxml.jackson.annotation.JsonIgnore
import concurrent.duration._
import de.pokerno.model.Countdown

trait Timers {
  
  @JsonIgnore val actingTimer       = new Countdown("acting", 30.seconds)
  @JsonIgnore val reconnectTimer    = new Countdown("reconnect", 15.seconds)
  @JsonIgnore val awayTimer         = new Countdown("away", 10.minutes)
  
}
