package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.protocol.wire

object Seat {
  object State extends Enumeration {
    type State = Value
    def state(name: String) = new Val(nextId, name)

    // no player
    val Empty = state("empty")
    // reserved seat
    val Taken = state("taken")
    // waiting next deal
    val Ready = state("ready")
    // waiting big blind
    val WaitBB = state("wait-bb")
    // posting big blind
    val PostBB = state("post-bb")
    // playing in current deal
    val Play = state("play")
    // all-in in current deal
    val AllIn = state("all-in")
    // did bet
    val Bet = state("bet")
    // did fold
    val Fold = state("fold")
    // autoplay
    val Auto = state("auto")
    // sit-out
    val Idle = state("idle")
    // disconnected
    val Away = state("away")
  }

  import State._
  type State = Value

  case class IsTaken() extends Exception("seat is taken")
}

class Seat(private var _state: Seat.State.State = Seat.State.Empty) {
  def state = _state

  private var _player: Option[Player] = None
  def player = _player
  def player_=(p: Player) {
    if (_state != Seat.State.Empty) throw Seat.IsTaken()

    _state = Seat.State.Taken
    _player = Some(p)
  }

  private var _amount: Decimal = .0
  def amount = _amount

  private def net(amt: Decimal) {
    // TODO: check < 0
    _amount += amt
    // FIXME
    if (_amount.toDouble == 0)
      _state = Seat.State.AllIn
  }
  
  def buyIn(amt: Decimal) {
    net(amt)
    _state = Seat.State.Ready
  }

  def wins(amt: Decimal) {
    net(amt)
  }

  private var _put: Decimal = .0
  def put = _put
  
  def put_=(amount: Decimal) {
    net(-amount)
    _put += amount
  }
  
  // total stack
  def stack = _put + _amount

  def clear() {
    _state = Seat.State.Empty
    _player = None
    _amount = .0
    _put = .0
  }

  /**
   * State transitions
   * */
  def play() {
    _state = Seat.State.Play
    _put = .0
  }

  def playing() {
    _state = Seat.State.Play
  }

  def idle() {
    _state = Seat.State.Idle
  }
  
  def away() {
    _state = Seat.State.Away
  }
  
  /**
   * Action
   * */
  // CHECK
  def canCheck(toCall: Decimal): Boolean = {
    _put == toCall
  }

  def check() {
    _state = Seat.State.Bet
  }

  // FOLD
  def canFold: Boolean = {
    inPlay
  }
  
  def fold() = {
    _state = Seat.State.Fold
    _put = .0
    .0
  }
  
  // ANTE, BRING_IN, SMALL_BLIND, BIG_BLIND, GUEST_BLIND, STRADDLE
  def canForce(amt: Decimal, toCall: Decimal): Boolean = {
    // TODO
    canCall(amt, toCall)
  }
  
  def force(amt: Decimal) {
    put = amt
    if (!isAllIn) _state = Seat.State.Play
  }

  // RAISE
  def canRaise(amt: Decimal, toRaise: Tuple2[Decimal, Decimal]): Boolean = {
    inPlay && _canRaise(amt, toRaise)
  }
  
  private def _canRaise(amt: Decimal, toRaise: Tuple2[Decimal, Decimal]): Boolean = {
    val (min, max) = toRaise
    amt <= amount && amt >= min && amt <= max
  }
  
  def raise(amt: Decimal) {
    put = amt - _put
    if (!isAllIn) _state = Seat.State.Bet
  }
  
  // CALL
  def canCall(amt: Decimal, toCall: Decimal): Boolean = {
    inPlay && _canCall(amt, toCall)
  }
  
  private def _canCall(amt: Decimal, toCall: Decimal): Boolean = {
    // call all-in
    (amt + _put < toCall && amt == amount ||
    // call exact amount
    amt + _put == toCall && amt <= amount)
  }
  
  def call(amt: Decimal) = {
    put += amt
    if (!isAllIn) _state = Seat.State.Bet
  }

  def didCall(amt: Decimal): Boolean = {
    isAllIn || _didCall(amt)
  }
  
  private def _didCall(amt: Decimal): Boolean = {
    amt <= _put
  }
  
  // BET
  def canBet: Boolean = {
    inPlay || isPostedBB
  }
  
  def postBet(bet: Bet) =
    bet.betType match {
      case Bet.Fold             ⇒ fold
      case Bet.Raise            ⇒ raise(bet.amount)
      case Bet.Call             ⇒ call(bet.amount)
      case Bet.Check            ⇒ check()
      case _: Bet.ForcedBet     ⇒ force(bet.amount)
    }

  // STATE
  def isEmpty =
    state == Seat.State.Empty
  
  def isReady =
    state == Seat.State.Ready || state == Seat.State.Play || state == Seat.State.Fold
  
  def isActive =
    state == Seat.State.Play || state == Seat.State.PostBB
  
  def isAllIn =
    state == Seat.State.AllIn
  
  def isWaitingBB =
    state == Seat.State.WaitBB
  
  def isPostedBB =
    state == Seat.State.PostBB
    
  def isPlaying =
    state == Seat.State.Play
  
  def inPlay =
    state == Seat.State.Play || state == Seat.State.Bet
  
  def inPot =
    inPlay || state == Seat.State.AllIn

  override def toString =
    if (_player.isDefined)
      "%s - %s (%.2f - %.2f)".format(_player get, _state, _amount, _put)
    else "(empty)"

}
