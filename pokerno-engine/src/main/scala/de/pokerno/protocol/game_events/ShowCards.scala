package de.pokerno.protocol.game_events

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class ShowCards(
    @JsonProperty var pos: Int,

    @JsonProperty var player: Player,

    @JsonProperty var cards: Cards,

    @JsonProperty var muck: Boolean = false
  ) extends GameEvent {}
