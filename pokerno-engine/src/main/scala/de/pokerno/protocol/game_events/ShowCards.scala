package de.pokerno.protocol.game_events

import beans._

sealed case class ShowCards(
    @BeanProperty var pos: Int,

    @BeanProperty var player: Player,

    @BeanProperty var cards: Cards,

    @BooleanBeanProperty var muck: Boolean = false
  ) extends GameEvent {}
