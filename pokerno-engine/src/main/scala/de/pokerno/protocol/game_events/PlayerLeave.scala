package de.pokerno.protocol.game_events

import beans._

sealed case class PlayerLeave(
    @BeanProperty var pos: Int,
    @BeanProperty var player: Player
) extends GameEvent {}
