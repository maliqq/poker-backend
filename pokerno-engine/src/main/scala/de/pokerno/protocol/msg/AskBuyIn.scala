package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}

import de.pokerno.model.seat.Position

sealed case class AskBuyIn(
    @JsonUnwrapped position: Position,
    @JsonProperty buy: Tuple2[Decimal, Decimal],
    @JsonProperty available: Decimal) {
}
