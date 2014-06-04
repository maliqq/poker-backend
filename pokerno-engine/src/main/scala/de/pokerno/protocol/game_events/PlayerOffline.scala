package de.pokerno.protocol.game_events

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class PlayerOffline(
    @JsonProperty var pos: Int,
    @JsonProperty var player: Player
) extends GameEvent {}
