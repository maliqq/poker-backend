package de.pokerno.model

import com.fasterxml.jackson.annotation.JsonInclude

object Position {
  implicit def seat2position(seat: Seat): Position = Position(seat.pos, seat.player)
  implicit def position2seat(position: Position)(implicit table: Table): Seat = {
    table.seats(position.pos)
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class Position(pos: Int, player: Option[Player]) {
}
