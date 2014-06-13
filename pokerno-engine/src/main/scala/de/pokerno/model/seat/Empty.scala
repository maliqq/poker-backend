package de.pokerno.model.seat

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.Seat
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import de.pokerno.model.SeatStateRef

object Empty {
  def apply(pos: Int) = new Empty(pos)
}

sealed class Empty(pos: Int) extends Seat(pos) {
  @JsonScalaEnumeration(classOf[SeatStateRef])  @JsonProperty def state = Seat.State.Empty
}
