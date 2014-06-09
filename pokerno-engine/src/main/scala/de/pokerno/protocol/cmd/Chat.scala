package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class Chat(
  @JsonProperty player: Player,
  @JsonProperty message: String
) extends Command {}
