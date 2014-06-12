package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}
import de.pokerno.model.Position

sealed case class TickTimer(
    @JsonUnwrapped position: Position,
    @JsonProperty var timeLeft: Int,
    @JsonProperty var timeBank: Boolean = false
  ) extends GameEvent {}
