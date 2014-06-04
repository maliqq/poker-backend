package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class Chat(
  @JsonProperty var player: Player,
  @JsonProperty var message: String
) extends Command {}
