package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonPropertyOrder}
import beans._

object Table {
  case class AlreadyJoined() extends Exception("Player already joined!")
}

@JsonPropertyOrder(Array("button","seats"))
class Table(@JsonIgnore val size: Int) {
  import Table._
  
  private val _seats = collection.mutable.MutableList.tabulate(size) { i => new Seat(i) }
  @JsonProperty def seats = _seats
  
  private val _button = new Ring(seats)
  @JsonProperty def button = _button
  def button_=(pos: Int) = _button.current = pos
  
  def seatsFrom(from: Int): Seq[Seat] = {
    val (before, after) = seats.zipWithIndex span (_._2 <= from)
    after.map(_._1) ++ before.map(_._1)
  }
  
  def fromButton() = seatsFrom(button)

  override def toString = {
    val b = new StringBuilder

    b.append("size=%d\n" format size)
    b.append("button=%d\n" format (button: Int))
    
    b.append(seats.zipWithIndex map { case (seat, index) ⇒
      "Seat %d: %s" format (index, seat.toString())
    } mkString "\n")
    
    b.toString()
  }
  
  private val _seating: collection.mutable.Map[Player, Int] = collection.mutable.Map.empty

  def takeSeat(at: Int, player: Player, amount: Option[Decimal] = None) = {
    if (hasPlayer(player))
      throw AlreadyJoined()
    val seat = seats(at)
    seat.player = player
    amount map (seat buyIn (_))
    addPlayer(at, player)
    seat
  }

  def clearSeat(pos: Int): Unit = {
    val seat = seats(pos)
    seat.player map(removePlayer(_))
    _seats(pos) = new Seat(pos)
  }

  def playerPos(player: Player): Option[Int] =    _seating.get(player)
  def hasPlayer(player: Player): Boolean =        _seating.contains(player)
  
  def playerSeat(player: Player): Option[Seat] =  playerPos(player) map(_seats(_))
  
  private def addPlayer(at: Int, player: Player): Unit =  _seating(player) = at
  private def removePlayer(player: Player): Unit =        _seating.remove(player)
  
  // gameplay logic
  def playStart() = seats foreach { seat =>
    if (seat.canPlay) seat.playing()
    else if (seat.isAllIn) seat.idle()
  }
  
  def roundComplete() = seats foreach { seat ⇒
    seat.clearAction()
    if (seat.inPot) seat.playing()
  }
  
  def playStop() = seats.foreach { seat =>
    seat.clearCards()
    if (seat.isFolded) seat.playing()
  }

}
