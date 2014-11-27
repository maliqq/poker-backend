package de.pokerno.model.seat.impl

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import de.pokerno.model.SeatStateRef
import de.pokerno.model.Seat.State

object Empty {
  def apply(pos: Int) = new Empty(pos)
}

sealed class Empty(pos: Int) extends Seat(pos) {
  @JsonScalaEnumeration(classOf[SeatStateRef])  @JsonProperty def state = State.Empty
}
