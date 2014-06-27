package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class AnnounceStart(
  @JsonProperty after: Long
) extends GameEvent {}
