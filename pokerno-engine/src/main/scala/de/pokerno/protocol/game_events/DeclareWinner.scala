package de.pokerno.protocol.game_events

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class DeclareWinner(
    @JsonProperty var pos: Int,

    @JsonProperty var player: Player,

    @JsonProperty var amount: Decimal
  ) extends GameEvent {}
