package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class SitOut(
  @JsonProperty player: Player
) extends Command {}
