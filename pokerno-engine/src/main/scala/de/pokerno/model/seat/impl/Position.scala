package de.pokerno.model.seat.impl

import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonGetter, JsonAutoDetect}

class Position(
    pos: Int,
    private val _player: Player
  ) extends Seat(pos) {
  
  // POS
  @JsonGetter def pos = _pos
  
  // PLAYER
  @JsonGetter def player: Player = _player
}
