package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.protocol.wire

object Seat {
  object State extends Enumeration {
    type State = Value
    def state(name: String) = new Val(nextId, name)
    
    val Empty = state("empty")
    val Taken = state("taken")
    val Ready = state("ready")
    val WaitBB = state("wait-bb")
    val PostBB = state("post-bb")
    val Play = state("play")
    val AllIn = state("all-in")
    val Bet = state("bet")
    val Fold = state("fold")
    val Auto = state("auto")
    val Idle = state("idle")
    val Away = state("away")
  }
  
  import State._
  type State = Value
  
  case class IsTaken() extends Exception("seat is taken")
}

class Seat {
  private var _state: Seat.State.State = Seat.State.Empty
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
    if (_state != Seat.State.Empty) throw Seat.IsTaken()

    _state = Seat.State.Taken
    _player = Some(p)
  }

  def clear {
    _state = Seat.State.Empty
    _player = None
    _amount = .0
    _put = .0
  }

  def play {
    _state = Seat.State.Play
    _put = .0
  }

  def playing {
    _state = Seat.State.Play
  }

  def check {
    _state = Seat.State.Bet
  }

  def fold = {
    _state = Seat.State.Fold
    _put = .0
    .0
  }

  def force(amount: Decimal) {
    put = amount
    _state = if (_amount == 0) Seat.State.AllIn else Seat.State.Play
  }

  def raise(amount: Decimal) {
    put = amount
    _state = if (_amount == 0) Seat.State.AllIn else Seat.State.Bet
  }

  def buyIn(amount: Decimal) {
    net(amount)
    _state = Seat.State.Ready
  }

  def wins(amount: Decimal) {
    net(amount)
  }

  def isCalled(amount: Decimal): Boolean = _state == Seat.State.AllIn || amount <= _put

  def post(bet: Bet) = bet.betType match {
    case Bet.Fold             ⇒ fold
    case Bet.Call | Bet.Raise ⇒ raise(bet.amount)
    case Bet.Check            ⇒ check
    case _: Bet.ForcedBet     ⇒ force(bet.amount)
  }

  private def net(amount: Decimal) {
    _amount += amount
  }

  def isReady = state == Seat.State.Ready || state == Seat.State.Play || state == Seat.State.Fold
  def isActive = state == Seat.State.Play || state == Seat.State.PostBB
  def isAllIn = state == Seat.State.AllIn
  def isWaitingBB = state == Seat.State.WaitBB
  def isPlaying = state == Seat.State.Play
  def inPlay = state == Seat.State.Play || state == Seat.State.Bet
  def inPot = inPlay || state == Seat.State.AllIn

  override def toString = if (_player.isDefined) "%s - %s (%.2f - %.2f)".format(_player get, _state, _amount, _put)
  else "(empty)"

}
