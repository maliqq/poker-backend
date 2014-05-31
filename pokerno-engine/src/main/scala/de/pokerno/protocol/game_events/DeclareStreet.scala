package de.pokerno.protocol.game_events

import beans._
import de.pokerno.gameplay.Street

sealed case class DeclareStreet(
    @BeanProperty val name: Street.Value
) extends GameEvent {}
