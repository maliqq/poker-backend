package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.{JsonInclude, JsonUnwrapped}

import de.pokerno.poker.Hand

object DeclareHand {
  def apply(pos: Int, player: Player, hand: Hand) = new DeclareHand(pos, player, hand)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclareHand(
    @JsonProperty pos: Integer,

    @JsonProperty player: Player,

    @JsonUnwrapped hand: Hand
  ) extends GameEvent {}
