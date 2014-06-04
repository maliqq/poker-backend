package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

sealed case class ShowCards(
    @JsonProperty var pos: Int,

    @JsonProperty var player: Player,

    @JsonSerialize(converter = classOf[Cards2Binary])
    @JsonProperty var cards: Cards,

    @JsonProperty var muck: Boolean = false
  ) extends GameEvent {}
