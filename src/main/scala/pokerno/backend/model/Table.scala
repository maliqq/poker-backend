package pokerno.backend.model

import scala.math.{BigDecimal => Decimal}

case class Player

trait Round[T] {
  def items: List[T]
  def size = items.size
  
  protected var _current: Int = 0
  
  def reset = {
    _current = 0
  }
  
  def current: T = items(_current)
  def current_=(at: Int) {
    _current = at
    _current %= size
  }
  def move = {
    _current += 1
    _current %= size
  }
}

trait Button extends Round[Seat] {
  def button = _current
  def button_=(at: Int) {
    current = at
  }
  def moveButton = move
}

trait Traverse {
  def traverse: List[Tuple2[Seat, Int]]
  
  def where(f: (Seat) => Boolean): List[Tuple2[Seat, Int]] =
    traverse filter { case (seat, _) => f(seat) }
}

class Table(var size: Int) extends Button with Traverse {
  val seats: List[Seat] = List.fill(size) { new Seat }
  def items = seats
  
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
