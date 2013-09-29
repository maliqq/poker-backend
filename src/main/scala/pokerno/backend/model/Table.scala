package pokerno.backend.model

import scala.math.{BigDecimal => Decimal}

case class Player

trait Button {
  def size: Int
  
  protected var _button: Int = 0
  def button = _button
  def button_=(at: Int) = {
    _button = at
    _button %= size
  }
  
  def moveButton = {
    _button += 1
    _button %= size
  }
}

trait Traverse {
  def traverse: List[Tuple2[Seat, Int]]
  
  def where(f: (Seat) => Boolean): List[Tuple2[Seat, Int]] =
    traverse filter { case (seat, _) => f(seat) }
  
  def active: List[Tuple2[Seat, Int]] =
    where { seat => seat.state == Seat.Play || seat.state == Seat.PostBB }

  def waiting: List[Tuple2[Seat, Int]] =
    where { seat => seat.state == Seat.WaitBB }
  
  def playing: List[Tuple2[Seat, Int]] =
    where { seat => seat.state == Seat.Play }
  
  def stillInPlay: List[Tuple2[Seat, Int]] =
    where { seat => seat.state == Seat.Play || seat.state == Seat.Bet }
  
  def stillInPot: List[Tuple2[Seat, Int]] =
    where { seat => seat.state == Seat.Play || seat.state == Seat.Bet || seat.state == Seat.AllIn }
}

class Table(var size: Int) extends Button with Traverse {
  val seats: List[Seat] = List.fill(size) { new Seat }
  
  def traverse = {
    val btn = seats(button)
    val (left, right) = seats.zipWithIndex.span(_._2 == button)
    List[Tuple2[Seat, Int]]((btn, button)) ++ left ++ right
  }
  
  private var _seating: Map[Player, Int] = Map.empty
  def addPlayer(player: Player, at: Int, amount: Decimal) {
  }
  
  def removePlayer(player: Player) {
  }
}
