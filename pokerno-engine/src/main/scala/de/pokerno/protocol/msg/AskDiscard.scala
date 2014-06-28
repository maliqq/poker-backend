package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}

import de.pokerno.model.table.seat.Position

sealed case class AskDiscard(
    @JsonUnwrapped position: Position
  ) extends GameEvent {}
