package de.pokerno.model.seat.impl

import com.fasterxml.jackson.annotation.JsonIgnore
import concurrent.duration._
import de.pokerno.model.Countdown

trait Timers {

  // timer on acting (bet, discard)
  @JsonIgnore val actingTimer       = new Countdown("acting", 1.minute)
  // timer on buy in
  @JsonIgnore val reserveTimer      = new Countdown("reserve", 1.minute)
  // timer on reconnect
  @JsonIgnore val reconnectTimer    = new Countdown("reconnect", 30.seconds)
  // timer on disconnected
  @JsonIgnore val awayTimer         = new Countdown("away", 10.minutes)
  // timer on sit out
  @JsonIgnore val sitOutTimer       = new Countdown("sitOut", 30.minutes)

}
