package de.pokerno.gameplay.betting

import de.pokerno.model._
import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonInclude}
import math.{ BigDecimal ⇒ Decimal }

@JsonInclude(JsonInclude.Include.NON_NULL)
class Round(
    table: Table,
    game: Game,
    stake: Stake
  ) {
  
  private var _acting = new Ring(table.seats)
  def acting = _acting
  def current = _acting.current
  
  @JsonIgnore val seats = table.seatsFrom(_acting.current)
  @JsonIgnore var bigBets: Boolean = false
  
  @JsonProperty val pot = new Pot
  @JsonProperty val rake: Option[SidePot] = None

  // limit number of raises per one street
  private final val MaxRaiseCount = 8
  private var raiseCount: Int = 0

  // current amount to call
  private var _call: Decimal = .0
  def call = _call
  
  // current raise range
  private var _raise: Option[MinMax[Decimal]] = None
  def raise = _raise
  
  def clear() {
    raiseCount = 0
    _call = .0
    _raise = None
    _acting.current = table.button
    // FIXME
    //pot.complete()
  }
  
  def complete() {
    table.seats.filter(_ inPlay) map { seat ⇒
      seat.play()
      seat.clearPut()
    }

    clear()
  }

  def forceBet(pos: Int, betType: Bet.ForcedType): Tuple2[Seat, Bet] = {
    _acting.current = pos
    val seat = table.seats(current)

    _call = stake amount betType

    val stack = seat.stack.get
    val amt = List(stack, _call) min
    val bet = betType match {
      case Bet.Ante =>        Bet.ante(amt)
      case Bet.SmallBlind =>  Bet.sb(amt)
      case Bet.BringIn =>     Bet.bringIn(amt)
      case Bet.Straddle =>    Bet.straddle(amt)
      case Bet.GuestBlind =>  Bet.gb(amt)
    }

    addBet(bet)
  }

  def requireBet(pos: Int): Seat = {
    _acting.current = pos
    
    val seat = table.seats(current)
    val limit = game.limit

    val blind = if (bigBets) stake.bigBlind * 2 else stake.bigBlind
    val total = seat.total

    if (total <= _call || raiseCount >= MaxRaiseCount)
      _raise = None
    else {
      val (min, max) = limit raise (total, blind + _call, pot.total)
      _raise = Some(MinMax(List(total, min) min, List(total, max) min))
    }
    
    seat
  }
  
  def addBet(_bet: Bet): Tuple2[Seat, Bet] = {
    val seat = table.seats(current)
    val player = seat.player.get

    val _posting = _bet match {
      case Bet.AllIn =>
        Bet.raise(seat.total)
      case Bet.Call(amt) if amt == 0 =>
        Bet.Call(List(_call, seat.total).min - seat.put.get)
      case _ =>
        _bet
    }
    
    if (!seat.canBet(_posting, stake, _call, _raise.get)) {
      Console printf("bet %s is not valid; call=%.2f raise=%s %s", _posting, _call, _raise, seat)
      Bet.fold
    }

    val diff = seat postBet _posting

    if (_posting.isActive) {
      if (_posting.isRaise)
        raiseCount += 1

      if (!_posting.isCall && seat.put.get > _call)
        _call = seat.put.get

      pot add (player, diff, seat.isAllIn)
    }

    (seat, _posting)
  }

}
