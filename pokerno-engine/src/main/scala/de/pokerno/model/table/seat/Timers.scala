package de.pokerno.model.table.seat

import com.fasterxml.jackson.annotation.JsonIgnore
import concurrent.duration._
import de.pokerno.model.Countdown

trait Timers {

  // timer on acting (bet, discard)
  @JsonIgnore val actingTimer       = new Countdown("acting", 30.seconds)
  // timer on buy in
  @JsonIgnore val reserveTimer      = new Countdown("reserve", 30.seconds)
  // timer on reconnect
  @JsonIgnore val reconnectTimer    = new Countdown("reconnect", 15.seconds)
  // timer on disconnected
  @JsonIgnore val awayTimer         = new Countdown("away", 10.minutes)
  
}
