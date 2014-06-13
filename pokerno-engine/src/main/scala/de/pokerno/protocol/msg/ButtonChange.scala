package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class ButtonChange(
    @JsonProperty pos: Int
) extends GameEvent {}
