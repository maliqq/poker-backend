package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class AskBuyIn(
    @JsonProperty pos: Int,
    @JsonProperty player: Player,
    @JsonProperty buy: Tuple2[Decimal, Decimal],
    @JsonProperty available: Decimal) {
}
