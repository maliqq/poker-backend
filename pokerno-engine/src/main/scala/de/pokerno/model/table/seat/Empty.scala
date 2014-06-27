package de.pokerno.model.table.seat

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import de.pokerno.model.table.SeatStateRef
import de.pokerno.model.table.Seat.State

object Empty {
  def apply(pos: Int) = new Empty(pos)
}

sealed class Empty(pos: Int) extends Seat(pos) {
  @JsonScalaEnumeration(classOf[SeatStateRef])  @JsonProperty def state = State.Empty
}
