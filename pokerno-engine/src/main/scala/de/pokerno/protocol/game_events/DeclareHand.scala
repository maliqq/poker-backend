package de.pokerno.protocol.game_events

import beans._
import com.fasterxml.jackson.annotation.JsonInclude

import de.pokerno.poker.Hand

object DeclareHand {
  def apply(pos: Int, player: Player, hand: Hand) = new DeclareHand(pos, player, hand)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class DeclareHand(
    @BeanProperty val pos: Integer,

    @BeanProperty val player: Player,

    _hand: Hand
  ) extends GameEvent {
  
  @BeanProperty val rank: String = _hand.rank.get.toString()
  
  @BeanProperty val cards: Cards = _hand.cards.value

  @BeanProperty val value: Cards = _hand.value
  
  @BeanProperty val high: Cards = _hand.high
  
  @BeanProperty val kicker: Cards = _hand.kicker
  
  @BeanProperty val description: String = _hand.description
  
}
