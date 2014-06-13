package de.pokerno.protocol.action

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class JoinTable(
    @JsonProperty pos: Int,
    @JsonProperty amount: Decimal
) extends PlayerEvent {}
