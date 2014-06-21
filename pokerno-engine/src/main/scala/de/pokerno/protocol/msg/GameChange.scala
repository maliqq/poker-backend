package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.{Game, GameType, GameLimit}

object GameChange {
  def apply(g: Game): GameChange = GameChange(g.`type`, g.limit)
}

sealed case class GameChange(
  @JsonProperty game: GameType,
  @JsonProperty limit: GameLimit
) extends GameEvent {}
