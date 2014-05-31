package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

object Table {
  case class AlreadyJoined() extends Exception("Player already joined!")
}

class Table(val size: Int) {
  import Table._

  val seats = collection.mutable.LinearSeq.fill(size) { new Seat }
  val button = new Ring(seats)
  
  def seatsFrom(from: Int): Seq[Tuple2[Seat, Int]] = {
    val (before, after) = seats.zipWithIndex span (_._2 <= from)
    after ++ before
  }
  
  def fromButton() = seatsFrom(button)

  override def toString = {
    val b = new StringBuilder

    b.append("size=%d\n" format size)
    b.append("button=%d\n" format (button: Int))
    
    b.append(seats.zipWithIndex map {
      case (seat, index) ⇒
        "Seat %d: %s" format (index, seat.toString())
    } mkString "\n")
    
    b.toString()
  }
  
  private val _seating: collection.mutable.Map[Player, Int] = collection.mutable.Map.empty

  type Box = (Player, Int)

  def boxes: List[Box] =
    seats.zipWithIndex.foldLeft(List[Box]()) {
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

  def clearSeat(pos: Int): Unit =
    seats(pos) = new Seat

  def playerPos(player: Player): Option[Int] =    _seating.get(player)
  def hasPlayer(player: Player): Boolean =        _seating.contains(player)
  def addPlayer(at: Int, player: Player): Unit =  _seating(player) = at
  def removePlayer(player: Player): Unit =        _seating.remove(player)

}
