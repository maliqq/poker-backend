package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonInclude, JsonUnwrapped}
import com.fasterxml.jackson.databind.annotation.JsonSerialize

import de.pokerno.model.Position

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DiscardCards(
    @JsonUnwrapped position: Position,

    @JsonSerialize(converter = classOf[Cards2Binary]) var cards: Cards = null,

    @JsonProperty cardsNum: Option[Int] = None
  ) extends GameEvent {}
