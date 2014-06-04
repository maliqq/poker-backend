package de.pokerno.protocol.game_events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DiscardCards(
    @JsonProperty var pos: Int,

    @JsonProperty var player: Player,

    @JsonProperty var cards: Cards = null,

    @JsonProperty var cardsNum: Option[Int] = None
  ) extends GameEvent {}
