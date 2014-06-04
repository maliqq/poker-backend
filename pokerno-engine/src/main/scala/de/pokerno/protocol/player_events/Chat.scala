package de.pokerno.protocol.player_events

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class Chat(
  @JsonProperty var message: String = null
) extends PlayerEvent {}
