package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

import org.slf4j.LoggerFactory
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonInclude, JsonAutoDetect, JsonPropertyOrder}
import de.pokerno.poker.{Cards, MaskedCards}

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.fasterxml.jackson.core.`type`.TypeReference

class SeatStateRef extends TypeReference[Seat.State.type]

object ActingSeat {
  implicit def seat2acting(seat: Seat): ActingSeat = ActingSeat(seat.pos, seat.player, seat.call, seat.raise)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class ActingSeat(
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

  case class IsTaken() extends Exception("seat is taken")
}

@JsonPropertyOrder(Array("state","player","stack","put","action"))
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
class Seat(
    private val _pos: Int = -1,
    private[model] var _state: Seat.State.Value = Seat.State.Empty
    )
      extends seat.States with seat.Actions with seat.Validations {

  import Seat._
  import seat.Callbacks._

  private[model] val log = LoggerFactory.getLogger(getClass)
  
  def this(_state: Seat.State.Value) = this(-1, _state)
  
  // reset before betting
  def reset() {
    _put = None
    _action = None
  }
  
  // POS
  def pos = _pos

  // STATE
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

  // PLAYER
  private[model] var _player: Option[Player] = None
  @JsonProperty def player = _player
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
  
  // PRESENCE
  private var _presence: Option[Presence.Value] = None
  @JsonIgnore def presence = _presence
  def presence_=(value: Option[Presence.Value]) {
    val old = _presence
    _presence = value
    presenceCallbacks.on(old, _presence)
  }

  private var _lastSeenOnline: Option[java.util.Date] = None
  def lastSeenOnline = _lastSeenOnline
  
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
  
  // STACK
  private var _stack: Option[Decimal] = None
  @JsonProperty def stack = _stack
  def stackAmount: Decimal = _stack.getOrElse(.0)

  // PUT
  private[model] var _put: Option[Decimal] = None
  @JsonProperty def put = _put
  def putAmount: Decimal = _put.getOrElse(.0)

  def total = stackAmount + putAmount
  def totalAmount = total

  def net(amt: Decimal, state: State.Value) {
    stackCallbacks.before(amt, stackAmount)
    // TODO: check < 0
    _stack = Some(amt + stackAmount)
    // FIXME
    _state = if (stackAmount == 0) State.AllIn else state
  }

  private val stackCallbacks = new Callbacks[Decimal]()
  stackCallbacks.bind(Before) { case _ ⇒
    if (isEmpty)
      throw new IllegalStateException("can't change amount, seat is empty; %s" format (this))
  }

  def buyIn(amt: Decimal): Unit =
    net(amt, State.Ready)

  def award(amt: Decimal): Unit =
    net(amt, State.Play)
    
  def wins(amt: Decimal) = award(amt)

  def put(amount: Decimal) {
    _put = Some(amount + putAmount)
  }
  
  def put(amount: Decimal, state: State.Value) {
    net(-amount, state)
    put(amount)
  }
  
  // ACTION
  private[model] var _action: Option[Bet] = None
  @JsonProperty def action = _action

  // RAISE
  private[model] var _raise: Option[Tuple2[Decimal, Decimal]] = None
  def raise = _raise
  def disableRaise() {
    _raise = None
  }
  def raise_=(range: Tuple2[Decimal, Decimal]) = {
    _raise = Some(range)
  }

  // CALL
  private[model] var _call: Option[Decimal] = None
  def call = _call
  def callAmount: Decimal = call.getOrElse(0)
  def call_=(amt: Decimal) = _call = Some(amt)

  // RAISE/CALL
  def notActing() {
    _raise = None
    _call = None
  }
  
  // CARDS
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
  
  @JsonProperty def cards = _cards.map { _.masked }
  def clearCards() = _cards = None

  override def toString =
    if (_player.isDefined)
      "%s - %s (%.2f - %.2f)".format(_player get, _state, stackAmount, putAmount)
    else "(empty)"

}
