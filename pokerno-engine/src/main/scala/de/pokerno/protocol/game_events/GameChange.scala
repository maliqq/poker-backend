package de.pokerno.protocol.game_events

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.Game

object GameChange {
  def apply(game: Game): GameChange = GameChange(game.game.toString(), game.limit.toString())
}

sealed case class GameChange(
  @JsonProperty game: String,
  
  @JsonProperty limit: String
) extends GameEvent {
  
}
