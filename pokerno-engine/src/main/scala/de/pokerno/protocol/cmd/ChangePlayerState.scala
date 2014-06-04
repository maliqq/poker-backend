package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.Seat

sealed case class ChangePlayerState(
  @JsonProperty var player: Player,
  @JsonProperty var state: Seat.State.Value
) extends Command {}
