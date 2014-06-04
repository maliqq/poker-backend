package de.pokerno.protocol.action

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.Bet

sealed case class AddBet(
  @JsonProperty var bet: Bet = null
) extends PlayerEvent {}
