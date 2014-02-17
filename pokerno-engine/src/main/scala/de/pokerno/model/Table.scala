package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import collection._
import de.pokerno.protocol.wire

case class Player(id: String) {
  override def toString = id
}

object Table {
  case class AlreadyJoined() extends Exception("Player already joined!")
}

class Table(val size: Int) {
  import Table._
  
  private val _seats = new Seats(List.fill(size) { new Seat })
  def seats = _seats
  def seatsAsList = _seats: List[Seat]

  private val _button = new Round(size)
  def button = _button

  override def toString = {
    val b = new StringBuilder

    b.append("size=%d\n" format size)
    b.append("button=%d\n" format (button: Int))
    b.append(_seats toString)

    b.toString()
  }

  private val _seating: mutable.Map[Player, Int] = mutable.Map.empty

  type Box = (Player, Int)

  def boxes = seatsAsList.filter(_.player.isDefined).zipWithIndex.map {
    case (seat, i) ⇒
      (seat.player.get, i)
  }

  def pos(player: Player): Option[Int] =
    _seating.get(player)

  def box(player: Player): Option[Box] = pos(player) map { i ⇒
    (player, i)
  }

  // TODO: seatWithPos
  def seat(player: Player): Option[(Seat, Int)] = pos(player) map { i ⇒
    (seatsAsList(i), i)
  }

  def addPlayer(at: Int, player: Player, amount: Option[Decimal] = None) {
    if (_seating.contains(player)) throw AlreadyJoined()
    val seat = seatsAsList(at)
    seat.player = player
    amount map (seat buyIn (_))
    _seating(player) = at
  }
  
  def removePlayer(player: Player) {
    _seating.remove(player)
  }

}
