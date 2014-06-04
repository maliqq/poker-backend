package de.pokerno.protocol.player_events

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class JoinTable(
    @JsonProperty var pos: Int,
    @JsonProperty var amount: Decimal
) extends PlayerEvent {}
