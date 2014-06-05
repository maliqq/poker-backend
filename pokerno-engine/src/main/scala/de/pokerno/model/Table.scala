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
  
  private val _seats = collection.mutable.LinearSeq.fill(size) { new Seat }
  def seats = _seats
  
  @JsonProperty("seats") def getSeats: List[Seat] = seats.toList

  private val _button = new Ring(seats)
  def button = _button
  def button_=(pos: Int) = _button.current = pos
  
  @JsonProperty("button") def getButton: Int = button
  
  def seatsFrom(from: Int): Seq[Tuple2[Seat, Int]] = {
    val (before, after) = seats.zipWithIndex span (_._2 <= from)
    after ++ before
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
