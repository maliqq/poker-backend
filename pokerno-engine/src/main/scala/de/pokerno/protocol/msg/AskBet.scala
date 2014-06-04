package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.MinMax

sealed case class AskBet(
    @JsonProperty var pos: Int,

    @JsonProperty var player: Player,

    @JsonProperty var call: Decimal,

    @JsonProperty var raise: MinMax[Decimal]
  ) extends GameEvent {}
