package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class ButtonChange(
    @JsonProperty var pos: Int
) extends GameEvent {}
