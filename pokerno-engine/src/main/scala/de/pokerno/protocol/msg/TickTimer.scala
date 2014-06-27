package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}
import de.pokerno.model.table.seat.Position

sealed case class TickTimer(
    @JsonUnwrapped position: Position,
    @JsonProperty timeLeft: Int,
    @JsonProperty timeBank: Boolean = false
  ) extends GameEvent {}
