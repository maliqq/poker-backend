package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}

sealed case class AskBet(
    pos: Int,
    player: Player,
    call: Decimal,
    raise: Option[Tuple2[Decimal,Decimal]]
  ) extends GameEvent {}
