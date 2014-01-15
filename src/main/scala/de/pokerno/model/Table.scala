package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import collection._
import de.pokerno.protocol.wire

case class Player(val id: String)

object Table {
  final val Waiting = wire.TableSchema.TableState.WAITING
  final val Active = wire.TableSchema.TableState.ACTIVE
  final val Paused = wire.TableSchema.TableState.PAUSED
  final val Closed = wire.TableSchema.TableState.CLOSED
}

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
