package de.pokerno.protocol.game_events

import beans._

sealed case class ButtonChange(
    @BeanProperty var pos: Int
) extends GameEvent {}
