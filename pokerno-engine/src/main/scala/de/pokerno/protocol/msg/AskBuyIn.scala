package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}
import de.pokerno.model.seat.impl.Position

sealed case class AskBuyIn(
    @JsonUnwrapped position: Position,
    @JsonProperty("buy_in") buyIn: Tuple2[Decimal, Decimal],
    @JsonProperty available: Decimal) extends GameEvent {}
