package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class ShowCards(
  @JsonProperty cards: Cards,
  @JsonProperty player: Player,
  @JsonProperty muck: Boolean = false
) extends Command {}
