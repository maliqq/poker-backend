package de.pokerno.protocol.player_events

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.Bet

sealed case class AddBet(
  @JsonProperty var bet: Bet = null
) extends PlayerEvent {}
