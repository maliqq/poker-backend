package de.pokerno.gameplay.betting

import org.slf4j.LoggerFactory
import de.pokerno.model._
import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonInclude}
import math.{ BigDecimal ⇒ Decimal }

@JsonInclude(JsonInclude.Include.NON_NULL)
class Round(@JsonIgnore table: Table, game: Game, stake: Stake) {
  private val log = LoggerFactory.getLogger(getClass)
  
  private var _current = table.button.current
  private var _oldCurrent = _current
  
  def current = _current
  def current_=(pos: Int) = {
    _current = pos
    pos
  }
  
  @JsonProperty def acting: Option[Acting] = _call map { call =>
    val seat = table.seats(_current)
    Acting(_current, seat.player, call, _raise)
  }
  
  private var _seats = table.seatsFrom(_current)
  def seats = {
    // update seats from new position
    if (_current != _oldCurrent) {
      _oldCurrent = _current
      _seats = table.seatsFrom(_current)
    }
    _seats
  }

  @JsonIgnore var bigBets: Boolean = false
  
  @JsonProperty val pot = new Pot
  @JsonProperty val rake: Option[SidePot] = None

  // limit number of raises per one street
  private final val MaxRaiseCount = 8
  private var raiseCount: Int = 0

  // current amount to call
  private var _call: Option[Decimal] = None
  def call = _call
  def callAmount: Decimal = _call.getOrElse(.0)
  
  // current raise range
  private var _raise: Option[Tuple2[Decimal, Decimal]] = None
  def raise = _raise
  
  def clear() {
    raiseCount = 0
    _call = None
    _raise = None
    _current = table.button.current
    // FIXME
    //pot.complete()
  }
  
  def calling() {
    if (_call.isEmpty) {
      _call = Some(0)
    }
  }
  
  def complete() {
    table.seats.filter(_ inPlay) map { seat ⇒
      seat.reset()
      seat.play()
    }

    clear()
  }

  def forceBet(pos: Int, betType: Bet.ForcedType): Tuple2[Seat, Bet] = {
    current = pos

    val seat = table.seats(current)
    val amount = stake amount betType
    
    _call = Some(amount)

    val stack = seat.stack.get
    val amt = List(stack, amount) min
    val bet = betType.force(amt)

    addBet(bet)
  }

  def requireBet(pos: Int): Seat = {
    current = pos
    
    calling()
    
    val seat = table.seats(current)
    val limit = game.limit

    val blind = if (bigBets) stake.bigBlind * 2 else stake.bigBlind
    val total = seat.total

    if (total <= callAmount || raiseCount >= MaxRaiseCount)
      _raise = None
    else {
      val (min, max) = limit raise (total, blind + callAmount, pot.total)
      _raise = Some(List(total, min) min, List(total, max) min)
    }
    
    seat
  }
  
  def addBet(_bet: Bet): Tuple2[Seat, Bet] = {
    val seat = table.seats(current)
    val player = seat.player.get

    var _posting = _bet match {
      case Bet.AllIn =>
        Bet.raise(seat.total)
      case Bet.Call(amt) if amt == null || amt == 0 =>
        Bet.Call(List(callAmount, seat.total).min - seat.putAmount)
      case _ =>
        _bet
    }
    
    if (!seat.canBet(_posting, stake, _call, _raise)) {
      log.warn("bet %s is not valid; call=%.2f raise=%s %s\n" format(_posting, callAmount, _raise, seat))
      _posting = Bet.fold
    }

    val diff = seat postBet _posting

    if (_posting.isActive) {
      if (_posting.isRaise)
        raiseCount += 1

      if (!_posting.isCall && seat.putAmount > callAmount)
        _call = Some(seat.putAmount)

      pot add (player, diff, seat.isAllIn)
    }

    (seat, _posting)
  }

}
