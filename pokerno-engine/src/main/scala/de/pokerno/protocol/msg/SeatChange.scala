package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonUnwrapped
import de.pokerno.model.Seat

sealed case class SeatChange(
  @JsonUnwrapped seat: Seat
)
