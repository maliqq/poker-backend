package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.Bet

sealed case class DiscardCards(
  @JsonProperty player: Player,
  @JsonProperty cards: Cards
) extends Command {}
