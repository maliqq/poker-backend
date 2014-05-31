package de.pokerno.protocol.game_events

import beans._

sealed case class TickTimer(
    @BeanProperty var pos: Int,
    @BeanProperty var player: Player,
    @BeanProperty var timeLeft: Int,
    @BeanProperty var timeBank: Boolean = false
  ) extends GameEvent {}
