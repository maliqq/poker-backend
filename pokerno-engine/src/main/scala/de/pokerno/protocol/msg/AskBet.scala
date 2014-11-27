package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}

import de.pokerno.model.seat.impl.{Acting, Position}

object AskBet {
  def apply(seat: Acting): AskBet = AskBet(seat.asPosition, seat.callAmount, seat.raise)
}

sealed case class AskBet(
    @JsonUnwrapped position: Position,
    @JsonProperty call: Decimal,
    @JsonProperty raise: Option[Tuple2[Decimal,Decimal]]
  ) extends GameEvent {}
