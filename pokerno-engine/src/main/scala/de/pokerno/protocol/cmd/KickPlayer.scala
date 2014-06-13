package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class KickPlayer(
  @JsonProperty player: Player
) extends Command {}
