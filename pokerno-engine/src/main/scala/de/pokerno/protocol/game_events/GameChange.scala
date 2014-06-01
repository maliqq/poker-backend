package de.pokerno.protocol.game_events

import beans._
import de.pokerno.model.Game

object GameChange {
  def apply(game: Game): GameChange = GameChange(game.game.toString(), game.limit.toString())
}

sealed case class GameChange(
  @BeanProperty game: String,
  
  @BeanProperty limit: String
) extends GameEvent {
  
}
