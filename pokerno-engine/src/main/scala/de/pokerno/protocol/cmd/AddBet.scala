package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.Bet

sealed case class AddBet(
  @JsonProperty var player: Player,
  @JsonProperty var bet: Bet
) extends Command {}
