package de.pokerno.protocol.commands

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class JoinPlayer(
  @JsonProperty var pos: Int,
  @JsonProperty var player: Player,
  @JsonProperty var amount: Decimal
) extends Command {}
