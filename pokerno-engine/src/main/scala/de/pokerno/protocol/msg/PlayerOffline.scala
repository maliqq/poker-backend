package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class PlayerOffline(
    @JsonProperty var pos: Int,
    @JsonProperty var player: Player
) extends GameEvent {}
