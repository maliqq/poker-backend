package de.pokerno.protocol.action

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class Chat(
  @JsonProperty message: String = null
) extends PlayerEvent {}
