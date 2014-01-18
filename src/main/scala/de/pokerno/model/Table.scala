package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import collection._
import de.pokerno.protocol.wire

case class Player(val id: String) {
  override def toString = id
}

object Table {
  final val Waiting = wire.TableSchema.TableState.WAITING
  final val Active = wire.TableSchema.TableState.ACTIVE
  final val Paused = wire.TableSchema.TableState.PAUSED
  final val Closed = wire.TableSchema.TableState.CLOSED
}

class Table(val size: Int) {
  private val _seats = new Seats(List.fill(size) { new Seat })
  def seats = _seats
  
  private val _button = new Round(size)
  def button = _button
  
  override def toString = {
    var b = new StringBuilder 
    
    b.append(_seats toString)
    b.append("button=%d" format(button))
    
    b.toString
  }

  private var _seating: mutable.Map[Player, Int] = mutable.Map.empty
  
  def addPlayer(player: Player, at: Int, amount: Option[Decimal] = None) {
    val seat = seats.asInstanceOf[List[Seat]](at)
    seat.player = player
    if (amount.isDefined) seat buyIn (amount get)
    _seating(player) = at
  }

  def removePlayer(player: Player) {
    val at = _seating(player)
    seats.asInstanceOf[List[Seat]](at).clear
    _seating remove (player)
  }
}
