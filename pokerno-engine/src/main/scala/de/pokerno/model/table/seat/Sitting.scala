package de.pokerno.model.table.seat

import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonIgnore, JsonIgnoreProperties, JsonInclude, JsonProperty, JsonPropertyOrder, JsonGetter}
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import de.pokerno.poker.{Cards, MaskedCards, cards2binary}
import de.pokerno.model.Bet
import de.pokerno.model.table.SeatStateRef
import de.pokerno.model.table.Seat.State
import de.pokerno.util.Colored._

@JsonPropertyOrder(Array("state","player","stack","put","action"))
@JsonIgnoreProperties(Array("pos", "raise", "call"))
class Sitting(
    _pos: Int,
    _player: Player,
    @JsonIgnore protected var _state: State.Value = State.Taken,
    private var _stack: Option[Decimal] = None
    ) extends Auto(_pos, _player) with States with Actions with Timers with Validations{
  import de.pokerno.model.table.Seat.Presence
  import Callbacks._
  
  def asActing: Acting = {
    val acting = new Acting(pos, player)
    call.map { acting.call = _ } 
    raise.map { acting.raise = _ }
    acting
  }
  
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
  @JsonGetter def stack = _stack
  def stackAmount: Decimal = _stack.getOrElse(.0)

  // PUT
  @JsonIgnore protected var _put: Option[Decimal] = None
  @JsonGetter def put = _put
  def putAmount: Decimal = _put.getOrElse(.0)

  // TOTAL
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
  @JsonGetter def action = _action
  
  def clearAction() {
    _put = None; _action = None; _call = None; _raise = None
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
  
  @JsonGetter def cards = _cards.map { _.masked }
  def clearCards() = {
    _cards = None
    _masks = Array()
  }
  
  override def toString = {
    val b = new StringBuilder
    b.append("Seat %d: %s (%s) [".format(pos, player, _state))
    b.append("%.2f - %.2f".format(stackAmount, putAmount))
    _call.map { v =>
      b.append(" / call: %.2f".format(v)) }
    _raise.map { v =>
      b.append(" / raise: %.2f..%.2f".format(v._1, v._2)) }
    b.append("]")
    b.toString
  }
}
