package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.tournament.Level

sealed case class LevelUp(
  @JsonProperty number: Int,
  @JsonProperty level: Level
) extends GameEvent {}
