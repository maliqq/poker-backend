package de.pokerno.model

import com.fasterxml.jackson.annotation.{JsonProperty, JsonInclude}

object Position {
  implicit def seat2position(seat: Seat): Position = Position(seat.pos, seat.player)
  implicit def position2seat(position: Position)(implicit table: Table): Seat = {
    table.seats(position.index)
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class Position(
    @JsonProperty("pos") index: Int,
    @JsonProperty player: Option[Player]
  ) {}
