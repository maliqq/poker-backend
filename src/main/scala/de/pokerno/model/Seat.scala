package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.backend.{protocol => proto}

object Seat {
  type State = proto.SeatSchema.SeatState
  
  final val Empty: State = proto.SeatSchema.SeatState.EMPTY
  final val Taken: State = proto.SeatSchema.SeatState.TAKEN
  final val Ready: State = proto.SeatSchema.SeatState.READY
  
  final val WaitBB: State = proto.SeatSchema.SeatState.POST_BB
  final val PostBB: State = proto.SeatSchema.SeatState.WAIT_BB
  
  final val Play: State = proto.SeatSchema.SeatState.PLAY
  final val AllIn: State = proto.SeatSchema.SeatState.ALL_IN
  final val Bet: State = proto.SeatSchema.SeatState.BET
  final val Fold: State = proto.SeatSchema.SeatState.FOLD
  final val Auto: State = proto.SeatSchema.SeatState.AUTO
  
  final val Idle: State = proto.SeatSchema.SeatState.IDLE
  final val Away: State = proto.SeatSchema.SeatState.AWAY
  
  case class IsTaken() extends Exception("seat is taken")
}

class Seat {
  private var _state: Seat.State = Seat.Empty
  def state = _state

  private var _amount: Decimal = .0
  def amount = _amount

  private var _put: Decimal = .0
  def put = _put
  def put_=(amount: Decimal) {
    net(_put - amount)
    _put = amount
  }

  def stack = _put + _amount

  private var _player: Option[Player] = None
  def player = _player
  def player_=(p: Player) {
    if (_state != Seat.Empty) throw Seat.IsTaken()

    _state = Seat.Taken
    _player = Some(p)
  }

  def clear {
    _state = Seat.Empty
    _player = None
    _amount = .0
    _put = .0
  }

  def play {
    _state = Seat.Play
    _put = .0
  }

  def playing {
    _state = Seat.Play
  }

  def check {
    _state = Seat.Bet
  }

  def fold = {
    _state = Seat.Fold
    _put = .0
    .0
  }

  def force(amount: Decimal) {
    put = amount
    _state = if (_amount == 0) Seat.AllIn else Seat.Play
  }

  def raise(amount: Decimal) {
    put = amount
    _state = if (_amount == 0) Seat.AllIn else Seat.Bet
  }

  def buyIn(amount: Decimal) {
    net(amount)
    _state = Seat.Ready
  }

  def wins(amount: Decimal) {
    net(amount)
  }

  def isCalled(amount: Decimal): Boolean = _state == Seat.AllIn || amount <= _put

  def post(bet: Bet) = bet.betType match {
    case Bet.Fold             ⇒ fold
    case Bet.Call | Bet.Raise ⇒ raise(bet.amount)
    case Bet.Check            ⇒ check
    case _     ⇒
      if (bet.isForced)
        force(bet.amount)
  }

  private def net(amount: Decimal) {
    _amount += amount
  }

  def isReady = state == Seat.Ready || state == Seat.Play || state == Seat.Fold
  def isActive = state == Seat.Play || state == Seat.PostBB
  def isWaitingBB = state == Seat.WaitBB
  def isPlaying = state == Seat.Play
  def inPlay = state == Seat.Play || state == Seat.Bet
  def inPot = inPlay || state == Seat.AllIn

  override def toString = if (_player.isDefined) "%s - %s (%.2f - %.2f)".format(_player get, _state, _amount, _put)
  else "(empty)"

}
