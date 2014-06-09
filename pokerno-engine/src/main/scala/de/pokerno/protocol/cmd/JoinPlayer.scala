package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class JoinPlayer(
  @JsonProperty pos: Int,
  @JsonProperty player: Player,
  @JsonProperty amount: Decimal
) extends Command {}
