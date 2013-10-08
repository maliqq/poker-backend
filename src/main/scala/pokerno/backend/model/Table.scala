package pokerno.backend.model

import scala.math.{ BigDecimal ⇒ Decimal }
import scala.collection._

case class Player(val id: String)

object Button {
  implicit def button2Int(button: Button): Int = button.current
}

class Button(val size: Int) extends Round(size)

class Table(val size: Int) {
  val seats = new Seats(size)
  val button = new Button(size)

  def seatsFromButton = seats.from(button)

  def seatAtButton: Tuple2[Seat, Int] = (seats(button), button)

  private var _seating: mutable.Map[Player, Int] = mutable.Map.empty
  def addPlayer(player: Player, at: Int, amount: Decimal) {
    seats(at) player = player
    seats(at) buyIn (amount)
    _seating(player) = at
  }

  def removePlayer(player: Player) {
    val at = _seating(player)
    seats(at).clear
    _seating remove (player)
  }
}
