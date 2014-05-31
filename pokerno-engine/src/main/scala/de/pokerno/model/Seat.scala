package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

object Seat {
  object State extends Enumeration {
    def state(name: String) = new Val(nextId, name)
    //def state[T <: Trait](name: String) = new Val(nextId, name) with T

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

  private var _lastSeenOnline: java.util.Date = null
  def lastSeenOnline = _lastSeenOnline

  def lastSeenOnlineBefore(millis: Long) = {
    _lastSeenOnline != null && _lastSeenOnline.before(new java.util.Date(millis))
  }

  def presence = _presence

  def presence_=(value: Option[Seat.Presence.Value]) {
    val old = _presence
    _presence = value
    presenceCallbacks.on(old, _presence)
  }

  class States[T](initial: T) {
    private val transitions = collection.mutable.HashMap[T, T]()
  }

  trait CallbackTopic
  case object Before extends CallbackTopic
  case object After extends CallbackTopic
  case object On extends CallbackTopic

  class Callbacks[T] {
    type cb = (T, T) ⇒ Unit
    private val _cb = collection.mutable.HashMap[CallbackTopic, collection.mutable.ListBuffer[cb]]()

    def bind(topic: CallbackTopic)(f: (T, T) ⇒ Unit) {
      if (!_cb.contains(topic)) _cb(topic) = collection.mutable.ListBuffer[cb]()
      _cb(topic) += f
    }

    def unbind(topic: CallbackTopic, f: (T, T) ⇒ Unit) {
      if (_cb.contains(topic))
        _cb(topic) -= f
    }

    def fire(topic: CallbackTopic, _old: T, _new: T) {
      _cb(topic).foreach { _(_old, _new) }
    }

    def clear(topic: CallbackTopic) {
      _cb(topic) = collection.mutable.ListBuffer[cb]()
    }

    def before(_old: T, _new: T): Unit = fire(Before, _old, _new)
    def after(_old: T, _new: T): Unit = fire(Before, _old, _new)
    def on(_old: T, _new: T): Unit = fire(Before, _old, _new)

  }

  val presenceCallbacks = new Callbacks[Option[Seat.Presence.Value]]()
  presenceCallbacks.bind(On) {
    case (_old, _new) ⇒
      _new match {
        case Some(Seat.Presence.Online) ⇒
          _lastSeenOnline = new java.util.Date()
        case _ ⇒ // nothing
      }
  }

  def offline() {
    if (!isEmpty) presence = Some(Seat.Presence.Offline)
  }

  def isOffline = presence match {
    case Some(Seat.Presence.Offline) ⇒ true
    case _                           ⇒ false
  }

  def online() {
    if (!isEmpty) presence = Some(Seat.Presence.Online)
  }

  def isOnline = _presence == Some(Seat.Presence.Online)

  // state
  def state = _state

  def state_=(newState: Seat.State.Value) {
    val _old = _state
    stateCallbacks.before(_old, newState)
    _state = newState
    //stateCallbacks.on(_old, _state)
    //stateCallbacks.after(_old, _state)
  }

  val stateCallbacks = new Callbacks[Seat.State.Value]()
  stateCallbacks.bind(Before) {
    case (_old, _new) ⇒
      _new match {
        case Seat.State.Play | Seat.State.Idle | Seat.State.Ready ⇒
          if (isEmpty)
            throw new IllegalStateException("can't change emtpy seat state: %s" format (this))

        case Seat.State.Away ⇒
          if (isEmpty || isOnline)
            throw new IllegalStateException("can't change seat state to away: %s (%s)" format (this, _presence))
      }
  }

  // player
  private var _player: Option[Player] = None
  def player = _player
  def player_=(p: Player) {
    playerCallbacks.before(_player.getOrElse(null), p)

    _state = Seat.State.Taken
    _player = Some(p)
  }
  val playerCallbacks = new Callbacks[Player]()
  playerCallbacks.bind(Before) {
    case _ ⇒ // FIXME move it to state callbacks
      if (_state != Seat.State.Empty)
        throw Seat.IsTaken()
  }

  // current stack
  private var _stack: Decimal = .0
  def stack = _stack
  def stackAmount = stack

  // current bet
  private var _lastAction: Bet.Value = null
  def lastAction = _lastAction

  private var _put: Decimal = .0
  def put = _put
  def putAmount = put

  // total stack
  def total = _stack + _put
  def totalAmoutn = total

  def net(amt: Decimal)(f: ⇒ Seat.State.Value) {
    stackCallbacks.before(amt, _stack)
    // TODO: check < 0
    _stack += amt
    // FIXME
    _state = if (_stack == 0) Seat.State.AllIn else f
  }

  val stackCallbacks = new Callbacks[Decimal]()
  stackCallbacks.bind(Before) {
    case _ ⇒
      if (isEmpty)
        throw new IllegalStateException("can't change amount, seat is empty; %s" format (this))
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

  def put(amount: Decimal)(f: ⇒ Seat.State.Value) {
    net(-amount)(f)
    _put += amount
  }
  def clearPut() {
    _put = 0
    _lastAction = null
  }

  /**
   * State transitions
   */
  def play(): Unit =
    state = Seat.State.Play

  def idle(): Unit =
    state = Seat.State.Idle

  def ready(): Unit =
    state = if (total == 0) Seat.State.Idle else Seat.State.Ready

  def away(): Unit =
    state = Seat.State.Away

  /**
   * Action
   */
  // CHECK
  def canCheck(toCall: Decimal): Boolean = {
    _put == toCall
  }

  def check(): Decimal = {
    _state = Seat.State.Bet
    _lastAction = Bet.Check
    .0
  }

  // FOLD
  def canFold: Boolean = {
    inPlay
  }

  def fold(): Decimal = {
    _state = Seat.State.Fold
    _put = .0
    _lastAction = Bet.Fold
    .0
  }

  // ANTE, BRING_IN, SMALL_BLIND, BIG_BLIND, GUEST_BLIND, STRADDLE
  def canForce(amt: Decimal, toCall: Decimal): Boolean = {
    // TODO
    canCall(amt, toCall)
  }

  def force(betType: Bet.Value, amt: Decimal): Decimal = {
    put(amt) {
      Seat.State.Play
    }
    _lastAction = betType
    amt
  }

  // RAISE
  def canRaise(amt: Decimal, toRaise: MinMax[Decimal]): Boolean = {
    inPlay && _canRaise(amt, toRaise)
  }

  private def _canRaise(amt: Decimal, toRaise: MinMax[Decimal]): Boolean = {
    amt <= total && amt >= toRaise.min && amt <= toRaise.max
  }

  def raise(amt: Decimal): Decimal = {
    val diff = amt - _put
    put(diff) {
      Seat.State.Bet
    }
    _lastAction = Bet.Raise
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
    _lastAction = Bet.Call
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
  
  def canBet(bet: Bet, stake: Stake, _call: Decimal, _raise: MinMax[Decimal]): Boolean =
    bet.betType match {
      case Bet.Fold ⇒
        canFold || notActive
  
      case Bet.Check ⇒
        canCheck(_call)
  
      case Bet.Call if isActive ⇒
        canCall(bet.amount.get, _call)
  
      case Bet.Raise if isActive ⇒
        canRaise(bet.amount.get, _raise)
  
      case f: Bet.ForcedBet ⇒
        canForce(bet.amount.get, stake.amount(f))
  
      case _ ⇒
        // TODO warn
        false
    }

  def postBet(bet: Bet): Decimal =
    bet.betType match {
      case Bet.Fold                         ⇒ fold
      case Bet.Raise if bet.isActive        ⇒ raise(bet.amount.get)
      case Bet.Call if bet.isActive         ⇒ call(bet.amount.get)
      case Bet.Check                        ⇒ check()
      case _: Bet.ForcedBet if bet.isActive ⇒ force(bet.betType, bet.amount.get)
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
