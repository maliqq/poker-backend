package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class DeclarePlayStart(
    @JsonProperty var play: PlayState
) extends GameEvent {}
