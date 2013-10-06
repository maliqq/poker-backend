package pokerno.backend.model

import scala.math.{ BigDecimal ⇒ Decimal }
import scala.collection._

case class Player(val name: String)

object Button {
  implicit def button2Int(button: Button): Int = button.current
}

class Button(val size: Int) extends Round(size)

class Table(size: Int) {
  val seats = new Seats(size)
  val button = new Button(size)
  
  def seatsFromButton = seats.from(button)
  
  private var _seating: mutable.Map[Player, Int] = mutable.Map.empty
  def addPlayer(player: Player, at: Int, amount: Decimal) {
    seats(at) player = player
    seats(at) amount = amount
    _seating(player) = at
  }

  def removePlayer(player: Player) {
    val at = _seating(player)
    seats(at).clear
    _seating remove (player)
  }
}
