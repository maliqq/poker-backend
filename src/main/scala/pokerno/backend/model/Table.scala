package pokerno.backend.model

import scala.math.{BigDecimal => Decimal}

case class Player

class Button(private var _pos: Int, val size: Int) {
  def pos = _pos
  def pos_=(at: Int) = this.synchronized {
    _pos = at
    _pos %= size
  }
  
  def move = this.synchronized{
    _pos += 1
    _pos %= size
  }
}

class Table(var size: Int) {
  implicit def buttonToInt(btn: Button): Int = btn.pos
  implicit def seatsToSeatList(seats: Seats): List[Seat] = seats.seats
  
  val seats: Seats = new Seats(size)
  val button: Button = new Button(0, size)
  
  def addPlayer(player: Player, at: Int, amount: Decimal) {
  }
  
  def removePlayer(player: Player) {
  }
}
