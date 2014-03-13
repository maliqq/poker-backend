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

  private val _seats = new Seats(collection.mutable.LinearSeq.fill(size) { new Seat })
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

  def boxes: List[Box] =
    seats.zipped.foldLeft(List[Box]()) {
      case (result, (seat, i)) ⇒
        if (seat.player.isDefined)
          (seat.player.get, i) :: result
        else result
    }

  def playerSeatWithPos(player: Player): Option[(Seat, Int)] =
    playerPos(player) map { i ⇒
      (seats(i), i)
    }

  def takeSeat(at: Int, player: Player, amount: Option[Decimal] = None) {
    if (hasPlayer(player))
      throw AlreadyJoined()
    val seat = seats(at)
    seat.player = player
    amount map (seat buyIn (_))
    addPlayer(at, player)
  }

  def clearSeat(pos: Int): Unit = _seats.clear(pos)

  def playerPos(player: Player): Option[Int] = _seating.get(player)
  def hasPlayer(player: Player): Boolean = _seating.contains(player)
  def addPlayer(at: Int, player: Player): Unit = _seating(player) = at
  def removePlayer(player: Player): Unit = _seating.remove(player)

}
