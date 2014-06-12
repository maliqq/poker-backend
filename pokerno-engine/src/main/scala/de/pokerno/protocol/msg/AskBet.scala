package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}

import de.pokerno.model.{ActingSeat, Position}

object AskBet {
  def apply(seat: ActingSeat): AskBet = AskBet(seat, seat.call.get, seat.raise)
}

sealed case class AskBet(
    @JsonUnwrapped position: Position,
    @JsonProperty call: Decimal,
    @JsonProperty raise: Option[Tuple2[Decimal,Decimal]]
  ) extends GameEvent {}
