package de.pokerno.model.seat.impl

import com.fasterxml.jackson.annotation.{JsonProperty, JsonInclude, JsonUnwrapped}

object Change {
  def from(seat: Sitting) = new Change(seat.pos, seat.player)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class Change(
  _pos: Int,
  _player: Player,
  
  @JsonProperty("stack") var stack: Option[Decimal] = None

  ) extends Position(_pos, _player) {
}
