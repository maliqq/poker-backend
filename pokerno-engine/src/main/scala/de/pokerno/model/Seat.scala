package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.protocol.wire

object Seat {
  object State extends Enumeration {
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
  
  object Presence extends Enumeration {
    val Online = new Val(nextId, "online")
    val Offline = new Val(nextId, "offline")
  }

  case class IsTaken() extends Exception("seat is taken")
}

class Seat(private var _state: Seat.State.Value = Seat.State.Empty) {
  // presence
  private var _presence: Option[Seat.Presence.Value] = None
  def presence = _presence.getOrElse(null)
  
  def offline() {
    if (!isEmpty) _presence = Some(Seat.Presence.Offline)
  }
  
  def isOffline = _presence == Some(Seat.Presence.Offline)
  
  def online() {
    if (!isEmpty) _presence = Some(Seat.Presence.Online)
  }
  
  def isOnline = _presence == Some(Seat.Presence.Online)
  
  // state
  def state = _state

  // player
  private var _player: Option[Player] = None
  def player = _player
  def player_=(p: Player) {
    if (_state != Seat.State.Empty) throw Seat.IsTaken()

    _state = Seat.State.Taken
    _player = Some(p)
  }

  // current stack
  private var _stack: Decimal = .0
  def stack = _stack
  def stackAmount = stack

  // current bet
  private var _put: Decimal = .0
  def put = _put
  def putAmount = put

  // total stack
  def total = _stack + _put
  def totalAmoutn = total

  def net(amt: Decimal)(f: => Seat.State.Value) {
    if (isEmpty)
      throw new IllegalStateException("can't change amount, seat is empty; %s" format(this)) 
    // TODO: check < 0
    _stack += amt
    // FIXME
    _state = if (_stack == 0)
      Seat.State.AllIn
    else f
  }

  def buyIn(amt: Decimal): Unit =
    net(amt) {
      Seat.State.Ready
    }

  def award(amt: Decimal): Unit =
    net(amt) {
      Seat.State.Play
    }
  def wins(amt: Decimal) = award(amt)

  def put(amount: Decimal)(f: => Seat.State.Value) {
    net(-amount)(f)
    _put += amount
  }
  def clearPut() = _put = 0

  def clear() {
    _state = Seat.State.Empty
    _player = None
    _stack = .0
    _put = .0
    _presence = None
  }

  /**
   * State transitions
   */
  def play() {
    if (isEmpty)
      throw new IllegalStateException("can't play, seat is empty: %s" format(this))
    _state = Seat.State.Play
  }

  def idle() {
    if (isEmpty)
      throw new IllegalStateException("can't change seat state to idle: %s" format(this))
    _state = Seat.State.Idle
  }

  def ready() {
    if (isEmpty)
      throw new IllegalStateException("can't change seat state to ready: %s" format(this))
    _state = if (total == 0) Seat.State.Idle else Seat.State.Ready
  }

  def away() {
    if (isEmpty || isOnline)
      throw new IllegalStateException("can't change seat state to away: %s (%s)" format(this, _presence))
    _state = Seat.State.Away
  }

  /**
   * Action
   */
  // CHECK
  def canCheck(toCall: Decimal): Boolean = {
    _put == toCall
  }

  def check(): Decimal = {
    _state = Seat.State.Bet
    .0
  }

  // FOLD
  def canFold: Boolean = {
    inPlay
  }

  def fold(): Decimal = {
    _state = Seat.State.Fold
    _put = .0
    .0
  }

  // ANTE, BRING_IN, SMALL_BLIND, BIG_BLIND, GUEST_BLIND, STRADDLE
  def canForce(amt: Decimal, toCall: Decimal): Boolean = {
    // TODO
    canCall(amt, toCall)
  }

  def force(amt: Decimal): Decimal = {
    put(amt) {
      Seat.State.Play
    }
    amt
  }

  // RAISE
  def canRaise(amt: Decimal, toRaise: Tuple2[Decimal, Decimal]): Boolean = {
    inPlay && _canRaise(amt, toRaise)
  }

  private def _canRaise(amt: Decimal, toRaise: Tuple2[Decimal, Decimal]): Boolean = {
    val (min, max) = toRaise
    amt <= total && amt >= min && amt <= max
  }

  def raise(amt: Decimal): Decimal = {
    val diff = amt - _put
    put(diff) {
      Seat.State.Bet
    }
    diff
  }

  // CALL
  def canCall(amt: Decimal, toCall: Decimal): Boolean = {
    inPlay && _canCall(amt, toCall)
  }

  private def _canCall(amt: Decimal, toCall: Decimal): Boolean = {
    // call all-in
    (amt + _put < toCall && amt == _stack ||
      // call exact amount
      amt + _put == toCall && amt <= _stack)
  }

  def call(amt: Decimal): Decimal = {
    put(amt) {
      Seat.State.Bet
    }
    amt
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

  import de.pokerno.util.ConsoleUtils._

  def postBet(bet: Bet): Decimal =
    bet.betType match {
      case Bet.Fold                         ⇒ fold
      case Bet.Raise if bet.isActive        ⇒ raise(bet.amount.get)
      case Bet.Call if bet.isActive         ⇒ call(bet.amount.get)
      case Bet.Check                        ⇒ check()
      case _: Bet.ForcedBet if bet.isActive ⇒ force(bet.amount.get)
      case x ⇒
        warn("unhandled postBet: %s", x)
        0
    }

  // STATE
  def isEmpty =
    state == Seat.State.Empty

  def isTaken =
    state == Seat.State.Taken

  def isReady =
    state == Seat.State.Ready
  
  def isPlaying =
    state == Seat.State.Play
  
  def isFold =
    state == Seat.State.Fold

  def isAllIn =
    state == Seat.State.AllIn

  def isWaitingBB =
    state == Seat.State.WaitBB

  def isPostedBB =
    state == Seat.State.PostBB
    
  def canPlayNextDeal =
    isReady || isPlaying || isFold

  def isActive =
    state == Seat.State.Play || state == Seat.State.PostBB

  def notActive =
    state == Seat.State.Away || state == Seat.State.Idle || state == Seat.State.Auto
    
  def inPlay =
    state == Seat.State.Play || state == Seat.State.Bet
  
//  def goesToShowdown =
//    state == Seat.State.Bet || state == Seat.State.AllIn
    
  def inPot =
    inPlay || state == Seat.State.AllIn

  override def toString =
    if (_player.isDefined)
      "%s - %s (%.2f - %.2f)".format(_player get, _state, _stack, _put)
    else "(empty)"

}
