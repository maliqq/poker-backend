package de.pokerno.protocol.game_events

import beans._
import com.fasterxml.jackson.annotation.{JsonInclude, JsonUnwrapped}

import de.pokerno.poker.Hand

object DeclareHand {
  def apply(pos: Int, player: Player, hand: Hand) = new DeclareHand(pos, player, hand)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclareHand(
    @BeanProperty pos: Integer,

    @BeanProperty player: Player,

    @JsonUnwrapped hand: Hand
  ) extends GameEvent {}
