package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonInclude, JsonUnwrapped}
import com.fasterxml.jackson.databind.annotation.JsonSerialize

import de.pokerno.poker.Hand
import de.pokerno.model.table.seat.Position

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclareHand(
    @JsonUnwrapped position: Position,
    
    @JsonSerialize(converter=classOf[Cards2Binary]) @JsonProperty cards: Cards,

    @JsonProperty hand: Hand
  ) extends GameEvent {}
