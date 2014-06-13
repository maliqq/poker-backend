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
  
  private val _seats: collection.mutable.MutableList[Seat] =
    collection.mutable.MutableList.tabulate(size) { i => new EmptySeat(i) }

  // BUTTON
  private val _button = new Ring(seats)
  @JsonProperty def button = _button
  def button_=(pos: Int) = _button.current = pos
  
  // SEATS
  @JsonProperty def seats = _seats
  def seatsFrom(from: Int): Seq[Seat] = {
    val (before, after) = seats.zipWithIndex span (_._2 <= from)
    after.map(_._1) ++ before.map(_._1)
  }
  def seatsFromButton() = seatsFrom(button)

  // SITTING
  def sitting: Seq[seat.Sitting] = _seats.filter(_.isInstanceOf[seat.Sitting]).asInstanceOf[Seq[seat.Sitting]]
  def sittingFrom(pos: Int) = seatsFrom(pos).filter(_.isInstanceOf[seat.Sitting]).asInstanceOf[Seq[seat.Sitting]]
  def sittingFromButton() = sittingFrom(button)
  
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

  def takeSeat(pos: Int, player: Player, amount: Option[Decimal] = None) = {
    if (hasPlayer(player))
      throw AlreadyJoined()
    val newSeat = new seat.Sitting(pos, player)
    amount map (newSeat buyIn (_))
    addPlayer(pos, player)
    _seats(pos) = newSeat
    newSeat
  }

  def clearSeat(pos: Int): Unit = seats(pos) match {
    case sitting: seat.Sitting =>
      removePlayer(sitting.player)
      _seats(pos) = new EmptySeat(pos)
    case _ =>
  }

  def playerPos(player: Player): Option[Int] =    _seating.get(player)
  def hasPlayer(player: Player): Boolean =        _seating.contains(player)
  
  def playerSeat(player: Player): Option[seat.Sitting] = playerPos(player) map { pos =>
    _seats(pos) match {
      case sitting: seat.Sitting => sitting
      case _ => return None
    }
  }
  
  private def addPlayer(at: Int, player: Player): Unit =  _seating(player) = at
  private def removePlayer(player: Player): Unit =        _seating.remove(player)
  
  // gameplay logic
  def playStart() = sitting foreach { seat =>
    if (seat.canPlay) seat.playing()
    //else if (seat.isAllIn) seat.idle()
  }
  
  def roundComplete() = sitting foreach { seat ⇒
    seat.clearAction()
    if (seat.inPot) seat.playing()
  }
  
  def playStop() = sitting foreach { seat =>
    seat.clearCards()
    if (seat.isAllIn) seat.idle()
    else if (seat.isFolded) seat.playing()
  }

}
