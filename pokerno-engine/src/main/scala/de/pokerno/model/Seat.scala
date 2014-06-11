package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

import org.slf4j.LoggerFactory
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonInclude, JsonAutoDetect, JsonPropertyOrder}
import beans._

import de.pokerno.poker.Cards

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.fasterxml.jackson.core.`type`.TypeReference

class SeatStateRef extends TypeReference[Seat.State.type]

object ActingSeat {
  implicit def seat2acting(seat: Seat): ActingSeat = ActingSeat(seat.pos, seat.player, seat.call, seat.raise)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
case class ActingSeat(
    @JsonProperty pos: Int,
    @JsonProperty player: Option[Player],
    @JsonProperty call: Option[Decimal],
    @JsonProperty raise: Option[Tuple2[Decimal, Decimal]]
)  {}

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

  trait StateTransitions {
    class States[T](initial: T) {
      private val transitions = collection.mutable.HashMap[T, T]()
    }
  }

  object Callbacks {

    trait CallbackTopic
    case object Before extends CallbackTopic
    case object After extends CallbackTopic
    case object On extends CallbackTopic

    class Callbacks[T] {
      type cb = (T, T) ⇒ Unit
      private val _cb = collection.mutable.HashMap[CallbackTopic, collection.mutable.ListBuffer[cb]]()

      def bind(topic: CallbackTopic)(f: (T, T) ⇒ Unit): Unit = {
        if (!_cb.contains(topic)) _cb(topic) = collection.mutable.ListBuffer[cb]()
        _cb(topic) += f
      }

      def unbind(topic: CallbackTopic, f: (T, T) ⇒ Unit): Unit = if (_cb.contains(topic))
        _cb(topic) -= f

      def fire(topic: CallbackTopic, _old: T, _new: T): Unit = if (_cb.contains(topic))
        _cb(topic).foreach { _(_old, _new) }

      def clear(topic: CallbackTopic): Unit =
        _cb(topic) = collection.mutable.ListBuffer[cb]()

      def before(_old: T, _new: T): Unit = fire(Before, _old, _new)
      def after(_old: T, _new: T): Unit = fire(After, _old, _new)
      def on(_old: T, _new: T): Unit = fire(On, _old, _new)

    }
  }

  case class IsTaken() extends Exception("seat is taken")
}

@JsonPropertyOrder(Array("state","player","stack","put","action"))
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
class Seat(
    private val _pos: Int = -1,
    private var _state: Seat.State.Value = Seat.State.Empty
    ) {

  private val log = LoggerFactory.getLogger(getClass)
  
  def this(_state: Seat.State.Value) = this(-1, _state)
  
  import Seat._
  import Seat.Callbacks._

  def pos = _pos
  
  // presence
  private var _presence: Option[Presence.Value] = None

  private var _lastSeenOnline: Option[java.util.Date] = None
  def lastSeenOnline = _lastSeenOnline
  
  // reset before betting
  def reset() {
    _put = None
    _lastAction = None
  }

  @JsonIgnore def presence = _presence

  def presence_=(value: Option[Presence.Value]) {
    val old = _presence
    _presence = value
    presenceCallbacks.on(old, _presence)
  }

  private val presenceCallbacks = new Callbacks[Option[Presence.Value]]()
  presenceCallbacks.bind(On) {
    case (_old, _new) ⇒
      _new match {
        case Some(Presence.Offline) ⇒
          _lastSeenOnline = Some(new java.util.Date())
        case _ ⇒ // nothing
      }
  }

  def offline() {
    if (!isEmpty) presence = Some(Presence.Offline)
  }

  def isOffline = presence match {
    case Some(Presence.Offline) ⇒ true
    case _                      ⇒ false
  }
  @JsonProperty("offline") def offlineStatus: java.lang.Boolean = if (isOffline) true else null

  def online() {
    if (!isEmpty) {
      presence = Some(Presence.Online)
      if (isAway) ready()
    }
  }

  def isOnline = _presence == Some(Presence.Online)
  //@JsonProperty("online") def onlineStatus: java.lang.Boolean = if (isOnline) true else null
  
  // state
  @JsonScalaEnumeration(classOf[SeatStateRef]) @JsonProperty("state") def state = _state

  def state_=(newState: State.Value) {
    val _old = _state
    stateCallbacks.before(_old, newState)
    _state = newState
    //stateCallbacks.on(_old, _state)
    //stateCallbacks.after(_old, _state)
  }

  private val stateCallbacks = new Callbacks[State.Value]()
  stateCallbacks.bind(Before) {
    case (_old, _new) ⇒
      _new match {
        case State.Play | State.Idle | State.Ready ⇒
          if (isEmpty)
            throw new IllegalStateException("can't change emtpy seat state: %s" format (this))

        case State.Away ⇒
          if (isEmpty || isOnline)
            throw new IllegalStateException("can't change seat state to away: %s (%s)" format (this, _presence))
      }
  }

  // player
  private var _player: Option[Player] = None
  @JsonProperty("player") def player = _player
  def player_=(p: Player) {
    playerCallbacks.before(_player.orNull, p)

    _state = State.Taken
    _player = Some(p)
  }
  private val playerCallbacks = new Callbacks[Player]()
  playerCallbacks.bind(Before) {
    case _ ⇒ // FIXME move it to state callbacks
      if (_state != State.Empty)
        throw Seat.IsTaken()
  }
  
  // cards dealt
  import de.pokerno.poker.MaskedCards
  private var _cards: Option[MaskedCards] = None
  private var _masks: Array[Boolean] = Array()
  
  def pocket(cards: Cards, hidden: Boolean) {
    _cards = if (_cards.isEmpty)
      Some(new MaskedCards(cards, hidden))
    else
      _cards.map { _ :+ (cards, hidden) }
  }
  
  def hole(cards: Cards) = pocket(cards, true) // hidden
  def door(cards: Cards) = pocket(cards, false) // visible
  
  def show(indexes: Array[Int]) {
    _cards.map { _./:(indexes) }
  }
  
  @JsonProperty("cards") def cards = _cards.map { _.masked }
  def clearCards() = _cards = None

  // current stack
  private var _stack: Option[Decimal] = None
  @JsonProperty("stack") def stack = _stack
  def stackAmount: Decimal = _stack.getOrElse(.0)

  // current bet
  private var _lastAction: Option[Bet] = None
  @JsonProperty("action") def lastAction = _lastAction

  private var _put: Option[Decimal] = None
  @JsonProperty("put") def put = _put
  def putAmount: Decimal = _put.getOrElse(.0)

  // total stack
  def total = stackAmount + putAmount
  def totalAmount = total

  def net(amt: Decimal)(f: ⇒ State.Value) {
    stackCallbacks.before(amt, stackAmount)
    // TODO: check < 0
    _stack = Some(amt + stackAmount)
    // FIXME
    _state = if (stackAmount == 0) State.AllIn else f
  }

  private val stackCallbacks = new Callbacks[Decimal]()
  stackCallbacks.bind(Before) {
    case _ ⇒
      if (isEmpty)
        throw new IllegalStateException("can't change amount, seat is empty; %s" format (this))
  }

  def buyIn(amt: Decimal): Unit =
    net(amt) {
      State.Ready
    }

  def award(amt: Decimal): Unit =
    net(amt) {
      State.Play
    }
  def wins(amt: Decimal) = award(amt)

  def put(amount: Decimal)(f: ⇒ State.Value) {
    net(-amount)(f)
    _put = Some(amount + putAmount)
  }

  /**
   * State transitions
   */
  def play(): Unit =
    state = State.Play

  def idle(): Unit =
    state = State.Idle

  def ready(): Unit =
    state = if (total == 0) State.Idle else State.Ready

  def away(): Unit =
    state = State.Away

  /**
   * Action
   */
  // CHECK
  def canCheck(toCall: Decimal): Boolean = {
    putAmount == toCall
  }

  def check(): Decimal = {
    _state = State.Bet
    _lastAction = Some(Bet.Check)
    .0
  }

  // FOLD
  def canFold: Boolean = {
    inPlay
  }

  def fold(): Decimal = {
    _state = State.Fold
    _put = None
    _lastAction = Some(Bet.Fold)
    .0
  }

  // ANTE, BRING_IN, SMALL_BLIND, BIG_BLIND, GUEST_BLIND, STRADDLE
  def canForce(amt: Decimal, toCall: Decimal): Boolean = {
    // TODO
    inPlay && _canCall(amt, toCall)
  }

  def force(bet: Bet.Forced): Decimal = {
    val amt = bet.amount
    put(amt) {
      State.Play
    }
    _lastAction = Some(bet)
    amt
  }
  
  def notActing() {
    _raise = None
    _call = None
  }

  // RAISE
  private var _raise: Option[Tuple2[Decimal, Decimal]] = None
  def raise = _raise
  
  def disableRaise() {
    _raise = None
  }
  
  def raise_=(range: Tuple2[Decimal, Decimal]) = {
    _raise = Some(range)
  }
  
  def canRaise(amt: Decimal): Boolean = {
    inPlay && _raise.map(_canRaise(amt, _)).getOrElse(false)
  }

  private def _canRaise(amt: Decimal, toRaise: Tuple2[Decimal, Decimal]): Boolean = {
    amt <= total && amt >= toRaise._1 && amt <= toRaise._2
  }
  
  def raise(amt: Decimal): Decimal = {
    val diff = amt - putAmount
    put(diff) {
      State.Bet
    }
    _lastAction = Some(Bet.Raise(amt))
    diff
  }

  // CALL
  private var _call: Option[Decimal] = None
  def call = _call
  
  def callAmount: Decimal = call.getOrElse(0)
  
  def canCall(amt: Decimal): Boolean = {
    inPlay &&
      _call.map(_canCall(amt, _)).getOrElse(false)
  }
  
  def call_=(amt: Decimal) = _call = Some(amt)
  
  def _canCall(amt: Decimal, toCall: Decimal) = {
    // call all-in
    amt + stackAmount < toCall && amt == stackAmount ||
      // call exact amount
      amt + putAmount == toCall && amt <= stackAmount
  }

  def call(amt: Decimal): Decimal = {
    put(amt) {
      State.Bet
    }
    _lastAction = Some(Bet.Call(amt))
    amt
  }

  def didCall(amt: Decimal): Boolean = {
    isAllIn || _didCall(amt)
  }

  private def _didCall(amt: Decimal): Boolean = {
    amt <= putAmount
  }

  // BET
  def canBet: Boolean = {
    inPlay || isPostedBB
  }
  
  def canBet(bet: Bet, stake: Stake): Boolean =
    bet match {
      case Bet.Fold ⇒
        canFold || notActive
  
      case Bet.Check ⇒
        _call.isEmpty || canCheck(_call.get)
  
      // FIXME check on null
      case Bet.Call(amt) if _call.isDefined && isActive ⇒
        canCall(amt)
  
      // FIXME check on null
      case Bet.Raise(amt) if _raise.isDefined && isActive ⇒
        canRaise(amt)
  
      case f: Bet.Forced ⇒
        canForce(f.amount, stake.amount(f.betType))
  
      case _ ⇒
        // TODO warn
        false
    }

  def postBet(bet: Bet): Decimal =
    bet match {
      case Bet.Fold                         ⇒ fold
      case Bet.Raise(amt) if amt > 0        ⇒ raise(amt)
      case Bet.Call(amt) if amt > 0         ⇒ call(amt)
      case Bet.Check                        ⇒ check()
      case f: Bet.Forced if f.amount > 0    ⇒ force(f)
      case x ⇒
        log.warn("unhandled postBet: {}", x)
        0
    }

  // STATE
  def isEmpty =
    state == State.Empty

  def isTaken =
    state == State.Taken

  def isReady =
    state == State.Ready
  
  def isAway =
    state == State.Away

  def isPlaying =
    state == State.Play

  def isFold =
    state == State.Fold

  def isAllIn =
    state == State.AllIn

  def isWaitingBB =
    state == State.WaitBB

  def isPostedBB =
    state == State.PostBB

  def canPlayNextDeal =
    isReady || isPlaying || isFold

  def isActive =
    state == State.Play || state == State.PostBB

  def notActive =
    state == State.Away || state == State.Idle || state == State.Auto

  def inPlay =
    state == State.Play || state == State.Bet

  //  def goesToShowdown =
  //    state == State.Bet || state == State.AllIn

  def inPot =
    inPlay || state == State.AllIn

  override def toString =
    if (_player.isDefined)
      "%s - %s (%.2f - %.2f)".format(_player get, _state, stackAmount, putAmount)
    else "(empty)"

}
