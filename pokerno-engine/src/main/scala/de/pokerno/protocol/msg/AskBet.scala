package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}

import de.pokerno.model.ActingSeat

object AskBet {
  def apply(seat: ActingSeat): AskBet = AskBet(seat.pos, seat.player.get, seat.call.get, seat.raise)
}

sealed case class AskBet(
    pos: Int,
    player: Player,
    call: Decimal,
    raise: Option[Tuple2[Decimal,Decimal]]
  ) extends GameEvent {}
