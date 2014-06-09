package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class AdvanceStack(
    @JsonProperty player: Player,
    @JsonProperty amount: Decimal
  ) extends Command {
}
