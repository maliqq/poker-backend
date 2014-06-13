package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}

import de.pokerno.model.Position

sealed case class DeclareWinner(
    @JsonUnwrapped position: Position,

    @JsonProperty amount: Decimal
  ) extends GameEvent {}
