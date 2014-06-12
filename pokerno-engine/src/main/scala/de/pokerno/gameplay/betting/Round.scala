package de.pokerno.gameplay.betting

import org.slf4j.LoggerFactory
import de.pokerno.model._
import de.pokerno.gameplay.{Context => Gameplay}
import de.pokerno.util.Colored._
import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonInclude}
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import math.{ BigDecimal ⇒ Decimal }

class Seat2Acting extends com.fasterxml.jackson.databind.util.StdConverter[Option[Seat], Option[ActingSeat]] {
  override def convert(seat: Option[Seat]): Option[ActingSeat] = seat.map(ActingSeat.seat2acting(_))
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Round(@JsonIgnore table: Table, game: Game, stake: Stake) {
  private val log = LoggerFactory.getLogger(getClass)
  
  private var _seats = table.fromButton
  def seats = _seats
  
  @JsonProperty def current = acting map(_.pos)
  
  private var _acting: Option[Seat] = None
  @JsonSerialize(converter = classOf[Seat2Acting]) def acting = _acting
  private def acting_=(seat: Seat) {
    _acting.map(_.notActing())
    _seats = table.seatsFrom(seat.pos)
    _acting = Some(seat)
  }
  
  @JsonIgnore var bigBets: Boolean = false
  
  @JsonProperty val pot = new Pot
  @JsonProperty val rake: Option[SidePot] = None

  // limit number of raises per one street
  private final val MaxRaiseCount = 8
  private var raiseCount: Int = 0

  // current amount to call
  private var _call: Option[Decimal] = None
  @JsonProperty def call = _call
  @JsonIgnore def call_=(amt: Decimal) = _call = Some(amt)
  def callAmount: Decimal = _call.getOrElse(.0)
  
  def reset() {
    raiseCount = 0
    _call = None
    _acting = None
    _seats = table.fromButton
    pot.complete()
  }
  
  def forceBet(seat: Seat, betType: Bet.ForcedType): Bet = {
    acting = seat

    val amount = stake amount betType
    
    call = amount
    seat.call = amount

    val stack = seat.stack.get
    val amt = List(stack, amount) min
    val bet = betType.force(amt)

    addBet(seat, bet)
  }

  def requireBet(seat: Seat) = {
    acting = seat
    
    seat.call = callAmount
    
    val limit = game.limit

    val blind = if (bigBets) stake.bigBlind * 2 else stake.bigBlind
    val total = seat.total

    if (total <= callAmount || raiseCount >= MaxRaiseCount)
      seat.disableRaise()
    else {
      val (min, max) = limit raise (total, blind + callAmount, pot.total)
      seat.raise = (List(total, min) min, List(total, max) min)
    }
  }
  
  // FIXME
  def addBet(_bet: Bet): Tuple2[Seat, Bet] = (acting.get, addBet(acting.get, _bet))
  
  def addBet(seat: Seat, _bet: Bet): Bet = {
    val player = seat.player.get

    var _posting = _bet match {
      case Bet.AllIn =>
        Bet.raise(seat.total)
      case Bet.Call(amt) if amt == null || amt == 0 =>
        Bet.Call(List(callAmount, seat.total).min - seat.putAmount)
      case _ =>
        _bet
    }
    
    if (!seat.canBet(_posting, stake)) {
      warn("bet %s is not valid; call=%.2f seat=%s\n", _posting, callAmount, seat)
      _posting = Bet.fold
    }

    val diff = seat postBet _posting

    if (_posting.isActive) {
      if (_posting.isRaise)
        raiseCount += 1

      if (!_posting.isCall && seat.putAmount > callAmount)
        call = seat.putAmount

      pot add (player, diff, seat.isAllIn)
    }

    _posting
  }

}
