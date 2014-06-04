package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonInclude}
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DiscardCards(
    @JsonProperty var pos: Int,

    @JsonProperty var player: Player,

    @JsonSerialize(converter = classOf[Cards2Binary]) var cards: Cards = null,

    @JsonProperty var cardsNum: Option[Int] = None
  ) extends GameEvent {}
