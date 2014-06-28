package de.pokerno.model.table.seat

import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonIgnore, JsonInclude, JsonProperty, JsonGetter}

class Acting(_pos: Int, _player: Player) extends Position(_pos, _player) {
  def asPosition: Position = new Position(pos, player)
  
  // RAISE
  @JsonIgnore protected var _raise: Option[Tuple2[Decimal, Decimal]] = None
  
  @JsonGetter def raise = _raise
  def disableRaise() {
    _raise = None
  }
  def raise_=(range: Tuple2[Decimal, Decimal]) = {
    _raise = Some(range)
  }

  // CALL
  @JsonIgnore protected var _call: Option[Decimal] = None
  
  @JsonGetter def call = _call
  def callAmount: Decimal = call.getOrElse(.0)
  def call_=(amt: Decimal) = _call = Some(amt)

  // RAISE/CALL
  def notBetting() {
    _raise = None
    _call = None
  }
  
}
