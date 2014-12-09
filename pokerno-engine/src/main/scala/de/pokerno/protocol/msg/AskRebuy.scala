package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}
import de.pokerno.model.seat.impl.Position

sealed case class AskRebuy(
    @JsonUnwrapped position: Position,
    @JsonProperty("rebuy") rebuy: Tuple2[Decimal, Decimal],
    @JsonProperty available: Decimal) extends GameEvent {}
