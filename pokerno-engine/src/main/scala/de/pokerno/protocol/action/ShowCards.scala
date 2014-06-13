package de.pokerno.protocol.action

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class ShowCards(
  @JsonProperty cards: Cards = null
) extends PlayerEvent {}
