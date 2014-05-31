package de.pokerno.protocol.game_events

import beans._
import com.fasterxml.jackson.annotation.JsonInclude

import de.pokerno.poker.Hand

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclareHand(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: Player,

    @BeanProperty var cards: Cards,

    @BeanProperty var hand: Hand
    ) extends GameEvent {}
