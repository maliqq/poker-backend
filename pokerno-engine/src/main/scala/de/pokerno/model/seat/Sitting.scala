package de.pokerno.model.seat

import math.{BigDecimal => Decimal}
import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonIgnore, JsonInclude, JsonProperty, JsonPropertyOrder}
import de.pokerno.poker.{Cards, MaskedCards, cards2binary}
import de.pokerno.model.{Player, Bet, Seat}
import de.pokerno.util.Colored._

@JsonPropertyOrder(Array("state","player","stack","put","action"))
class Sitting(pos: Int, private var _player: Player) extends Seat(pos) with States with Actions with Validations{
  import Seat._
  import Callbacks._
  
  // STATE
  
  stateCallbacks.bind(Before) {
    case (_old, _new) ⇒
      warn("seat %s state change: %s -> %s", this, _old, _new)
      _new match {
        case State.Play | State.Idle | State.Ready ⇒
          if (isEmpty) throw new IllegalStateException("can't change emtpy seat state: %s" format (this))

        case State.Away ⇒
          if (isOnline) throw new IllegalStateException("can't change seat state to away: %s (%s)" format (this, _presence))
        
        case _ =>
      }
  }
  
  // PLAYER
  @JsonProperty def player = _player
  
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
  
  def clearAction() {
    _put = None; _action = None; _call = None; _raise = None
  }
  
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

  override def toString = {
    val b = new StringBuilder
    b.append("%s (%s) [".format(_player, _state))
    b.append("%.2f - %.2f".format(stackAmount, putAmount))
    if (_call.isDefined) b.append(" / call: %.2f".format(_call.get))
    if (_raise.isDefined) b.append(" / raise: %.2f..%.2f".format(_raise.get._1, _raise.get._2))
    b.append("]")
    b.toString
  }
}
