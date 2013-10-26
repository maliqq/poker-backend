package de.pokerno.backend.model

import scala.math.{ BigDecimal ⇒ Decimal }
import scala.collection._

case class Player(val id: String)

class Table(val size: Int) {
  val seats = new Seats(size)
  val button = new Round(size)

  private var _seating: mutable.Map[Player, Int] = mutable.Map.empty
  def addPlayer(player: Player, at: Int, amount: Option[Decimal] = None) {
    seats(at) player = player
    if (amount.isDefined) seats(at) buyIn (amount get)
    _seating(player) = at
  }

  def removePlayer(player: Player) {
    val at = _seating(player)
    seats(at).clear
    _seating remove (player)
  }
}
