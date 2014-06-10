package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonInclude, JsonUnwrapped}
import com.fasterxml.jackson.databind.annotation.JsonSerialize

import de.pokerno.poker.Hand

object DeclareHand {
  def apply(pos: Int, player: Player, cards: Cards, hand: Hand) = new DeclareHand(pos, player, cards, hand)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclareHand(
    @JsonProperty pos: Integer,

    @JsonProperty player: Player,
    
    @JsonSerialize(converter=classOf[Cards2Binary]) @JsonProperty cards: Cards,

    @JsonProperty hand: Hand
  ) extends GameEvent {}
