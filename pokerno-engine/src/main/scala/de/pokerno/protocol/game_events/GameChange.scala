package de.pokerno.protocol.game_events

import beans._
import de.pokerno.model.Game

sealed case class GameChange(
    @BeanProperty val game: Game
) extends GameEvent {}
