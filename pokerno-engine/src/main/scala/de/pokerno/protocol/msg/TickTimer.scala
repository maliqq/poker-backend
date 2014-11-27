package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}
import de.pokerno.model.seat.impl.Position

sealed case class TickTimer(
    @JsonUnwrapped position: Position,
    @JsonProperty timeLeft: Int,
    @JsonProperty timeBank: Boolean = false
  ) extends GameEvent {}
