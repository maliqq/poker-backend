package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.seat.impl.Change

sealed case class SeatChange(
  @JsonProperty("seat") change: Change
) extends GameEvent
