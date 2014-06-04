package de.pokerno.protocol.commands

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class KickPlayer(
  @JsonProperty var player: Player
) extends Command {}
