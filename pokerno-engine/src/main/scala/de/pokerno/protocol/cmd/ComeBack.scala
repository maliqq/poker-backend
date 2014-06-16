package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class ComeBack(
  @JsonProperty player: Player
) extends Command {}
