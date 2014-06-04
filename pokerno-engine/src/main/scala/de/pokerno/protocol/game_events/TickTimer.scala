package de.pokerno.protocol.game_events

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class TickTimer(
    @JsonProperty var pos: Int,
    @JsonProperty var player: Player,
    @JsonProperty var timeLeft: Int,
    @JsonProperty var timeBank: Boolean = false
  ) extends GameEvent {}
