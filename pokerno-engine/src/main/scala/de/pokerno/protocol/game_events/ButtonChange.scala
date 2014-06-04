package de.pokerno.protocol.game_events

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class ButtonChange(
    @JsonProperty var pos: Int
) extends GameEvent {}
