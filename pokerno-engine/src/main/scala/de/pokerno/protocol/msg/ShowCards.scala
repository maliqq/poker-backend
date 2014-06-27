package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.pokerno.model.table.seat.Position

sealed case class ShowCards(
    @JsonUnwrapped position: Position,

    @JsonSerialize(converter = classOf[Cards2Binary])
    @JsonProperty cards: Cards,

    @JsonProperty muck: Boolean = false
  ) extends GameEvent {}
