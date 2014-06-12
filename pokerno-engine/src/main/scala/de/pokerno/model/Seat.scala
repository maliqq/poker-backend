package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

import org.slf4j.LoggerFactory
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonInclude, JsonAutoDetect, JsonPropertyOrder}
import de.pokerno.poker.{Cards, MaskedCards}
import de.pokerno.util.Colored._

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.fasterxml.jackson.core.`type`.TypeReference

class SeatStateRef extends TypeReference[Seat.State.type]

object ActingSeat {
  implicit def seat2acting(seat: Seat): ActingSeat = ActingSeat(seat.pos, seat.player, seat.call, seat.raise)
  implicit def acting2position(acting: ActingSeat): Position = Position(acting.pos, acting.player)
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
    initialState: Seat.State.Value = Seat.State.Empty 
    )
      extends seat.States with seat.Actions with seat.Validations {

  import Seat._
  import seat.Callbacks._

  @JsonIgnore protected val log = LoggerFactory.getLogger(getClass)
  
  @JsonIgnore protected var _state: State.Value = initialState
  def this(_state: Seat.State.Value) = this(-1, _state)
  
  def clearAction() {
    _put = None; _action = None; _call = None; _raise = None
  }
  
  // POS
  def pos = _pos

  // STATE
  @JsonScalaEnumeration(classOf[SeatStateRef]) @JsonProperty def state = _state

  def state_=(_new: State.Value) {
    val _old = _state
    if (_old != _new) {
      stateCallbacks.before(_old, _new)
      _state = _new
    }
    //stateCallbacks.on(_old, _state)
    //stateCallbacks.after(_old, _state)
  }

  private val stateCallbacks = new Callbacks[State.Value]()
  stateCallbacks.bind(Before) {
    case (_old, _new) ⇒
      warn("seat %s state change: %s -> %s", this, _old, _new)
      _new match {
        case State.Play | State.Idle | State.Ready ⇒
          if (isEmpty)
            throw new IllegalStateException("can't change emtpy seat state: %s" format (this))

        case State.Away ⇒
          if (isEmpty || isOnline)
            throw new IllegalStateException("can't change seat state to away: %s (%s)" format (this, _presence))
        
        case _ =>
      }
  }

  // PLAYER
  private var _player: Option[Player] = None
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
  def hasPlayer(p: Player): Boolean = player.map(_ == p).getOrElse(false)
  
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
  @JsonIgnore protected var _put: Option[Decimal] = None
  @JsonProperty def put = _put
  def putAmount: Decimal = _put.getOrElse(.0)

  def total = stackAmount + putAmount
  def totalAmount = total

  def net(amt: Decimal) {
    val amount = stackAmount + amt
    stackCallbacks.before(stackAmount, amount)
    // TODO: check < 0
    _stack = Some(amount)
  }

  private val stackCallbacks = new Callbacks[Decimal]()
  stackCallbacks.bind(Before) { case (_old, _new) ⇒
    if (isEmpty)
      throw new IllegalStateException("can't change amount, seat is empty; %s" format (this))
    if (_new < 0)
      throw new IllegalStateException("amount < 0; %s" format (this))
  }

  def buyIn(amt: Decimal) = {
    net(amt)
    ready()
  }
  
  def wins(amt: Decimal) = {
    net(amt)
    playing()
  }

  def puts(amt: Decimal) {
    net(-amt)
    _put = Some(amt + putAmount)
  }
  
  // ACTION
  @JsonIgnore protected var _action: Option[Bet] = None
  @JsonProperty def action = _action

  // RAISE
  @JsonIgnore protected var _raise: Option[Tuple2[Decimal, Decimal]] = None
  @JsonIgnore def raise = _raise
  def disableRaise() {
    _raise = None
  }
  def raise_=(range: Tuple2[Decimal, Decimal]) = {
    _raise = Some(range)
  }

  // CALL
  @JsonIgnore protected var _call: Option[Decimal] = None
  @JsonIgnore def call = _call
  def callAmount: Decimal = call.getOrElse(.0)
  def call_=(amt: Decimal) = _call = Some(amt)
  // ALL IN
  def allIn: Option[Decimal] = {
    if (isAllIn) put
    else None
  }
  def allInAmount: Decimal = allIn.getOrElse(.0)

  // RAISE/CALL
  def notActing() {
    _raise = None
    _call = None
  }
  
  // CARDS
  private var _cards: Option[MaskedCards] = None
  private var _masks: Array[Boolean] = Array()
  
  private def pocket(cards: Cards, hidden: Boolean) {
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
  def clearCards() = {
    _cards = None
    _masks = Array()
  }

  override def toString =
    if (_player.isDefined) {
      val b = new StringBuilder
      b.append("%s (%s) [".format(_player get, _state))
      b.append("%.2f - %.2f".format(stackAmount, putAmount))
      if (_call.isDefined) b.append(" / call: %.2f".format(_call.get))
      if (_raise.isDefined) b.append(" / raise: %.2f..%.2f".format(_raise.get._1, _raise.get._2))
      b.append("]")
      b.toString
    } else "(empty)"

}
