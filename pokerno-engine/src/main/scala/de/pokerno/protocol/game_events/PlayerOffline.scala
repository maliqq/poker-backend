package de.pokerno.protocol.game_events

import beans._

sealed case class PlayerOffline(
    @BeanProperty var pos: Int,
    @BeanProperty var player: Player
) extends GameEvent {}
