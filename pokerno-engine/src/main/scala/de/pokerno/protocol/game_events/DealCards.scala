package de.pokerno.protocol.game_events

import beans._
import com.fasterxml.jackson.annotation.JsonInclude

import de.pokerno.model.DealType

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DealCards(
    @BeanProperty var `type`: DealType.Value,

    @BeanProperty var cards: Array[Byte] = Array(),

    @BeanProperty var pos: Option[Int] = None,

    @BeanProperty var player: Option[Player] = None,

    @BeanProperty var cardsNum: Option[Int] = None
  ) extends GameEvent {}
