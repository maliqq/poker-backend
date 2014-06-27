package de.pokerno.model

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonPropertyOrder}
import beans._

object Table {
  case class AlreadyJoined() extends Exception("Player already joined!")
}

@JsonPropertyOrder(Array("button","seats"))
class Table(@JsonIgnore val size: Int) {
  import Table._
  import table.seat._
  
  private val _seats: collection.mutable.MutableList[table.Seat] =
    collection.mutable.MutableList.tabulate(size) { i => Empty(i) }

  // BUTTON
  private val _button = new Ring(seats)
  @JsonProperty def button = _button
  def button_=(pos: Int) = _button.current = pos
  
  // SEATS
  @JsonProperty def seats = _seats
  def seatsFrom(from: Int): Seq[table.Seat] = {
    val (before, after) = seats.zipWithIndex span (_._2 <= from)
    after.map(_._1) ++ before.map(_._1)
  }
  def seatsFromButton() = seatsFrom(button)

  // SITTING
  def sitting: Seq[Sitting] = _seats.filter(_.isInstanceOf[Sitting]).asInstanceOf[Seq[Sitting]]
  def sittingFrom(pos: Int) = seatsFrom(pos).filter(_.isInstanceOf[Sitting]).asInstanceOf[Seq[Sitting]]
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

  def take(pos: Int, player: Player, amount: Option[Decimal] = None) = {
    if (has(player))
      throw AlreadyJoined()
    val seat = new Sitting(pos, player)
    amount map (seat buyIn (_))
    add(pos, player)
    _seats(pos) = seat
    seat
  }

  def clear(pos: Int): Unit = seats(pos) match {
    case seat: Sitting =>
      remove(seat.player)
      _seats(pos) = Empty(pos)
    case _ =>
  }

  def indexOf(player: Player): Option[Int]    = _seating.get(player)
  def pos(player: Player): Option[Int]        = indexOf(player)

  def contains(player: Player): Boolean       = _seating.contains(player)
  def has(player: Player): Boolean            = contains(player)
  
  def apply(player: Player): Option[Sitting] = pos(player) map { pos =>
    _seats(pos) match {
      case seat: Sitting => seat
      case _ => return None
    }
  }
  
  private def add(at: Int, player: Player): Unit =  _seating(player) = at
  private def remove(player: Player): Unit =        _seating.remove(player)
  
}
