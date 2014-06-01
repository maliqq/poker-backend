package de.pokerno.protocol.game_events

import beans._
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DiscardCards(
    @BeanProperty var pos: Int,

    @BeanProperty var player: Player,

    @BeanProperty var cards: Cards = null,

    @BeanProperty var cardsNum: Option[Int] = None
  ) extends GameEvent {}
