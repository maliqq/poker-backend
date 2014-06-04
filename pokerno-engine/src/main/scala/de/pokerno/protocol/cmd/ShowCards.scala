package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class ShowCards(
  @JsonProperty var cards: Cards,
  @JsonProperty var player: Player,
  @JsonProperty var muck: Boolean = false
) extends Command {}
