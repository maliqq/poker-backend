package pokerno.backend.engine

import pokerno.backend.model._

class BettingRound(val table: Table) extends Round(table.size) {
  current = table.button
  
  def seats = table.seats.from(current)
  private var _acting: Tuple2[Seat, Int] = null
  
  def acting = _acting
  def acting_=(act: Tuple2[Seat, Int]) {
    _acting = act
    current = act._2
  }
}
