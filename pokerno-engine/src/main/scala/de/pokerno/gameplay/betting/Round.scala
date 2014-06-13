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
  
  private def acting(seat: Seat, call: Decimal) {
    _acting.map(_.notActing())
    _seats = table.seatsFrom(seat.pos)
    _acting = Some(seat)
    seat.call = call
  }
  
  def reset() {
    raiseCount = 0
    _acting = None
    _seats = table.fromButton
    pot.complete()
  }
  
  @JsonIgnore var bigBets: Boolean = false
  
  @JsonProperty val pot = new Pot
  @JsonProperty val rake: Option[SidePot] = None

  // limit number of raises per one street
  private final val MaxRaiseCount = 8
  private var raiseCount: Int = 0

  // current amount to call
  def callAmount: Decimal = _seats.map(_.callAmount).max
  // current amount to call among all-ins
  def allInAmount: Decimal = _seats.map(_.allInAmount).max
  
  def forceBet(seat: Seat, betType: Bet.ForcedType): Bet = {
    val amount = stake amount betType
    
    acting(seat, amount)

    val stack = seat.stack.get
    val amt = List(stack, amount) min
    val bet = betType.force(amt)

    addBet(seat, bet)
  }

  def requireBet(seat: Seat) = {
    val amount = callAmount
    
    acting(seat, amount)
    
    val total = seat.total
    if (total <= amount || raiseCount >= MaxRaiseCount)
      seat.disableRaise()
    else {
      val limit = game.limit
      val blind = if (bigBets) stake.bigBlind * 2 else stake.bigBlind
      val (min, max) = limit raise (total, blind + amount, pot.total)
      seat.raise = (List(total, min) min, List(total, max) min)
    }
  }
  
  def addBet(seat: Seat, _bet: Bet): Bet = {
    val player = seat.player.get

    val posting = {
      val _posting = seat.posting(_bet)
    
      if (seat.canBet(_posting, stake)) _posting
      else {
        warn("bet %s is not valid; call=%.2f seat=%s\n", _posting, callAmount, seat)
        Bet.fold
      }
    }

    val diff = seat postBet posting

    if (posting.isActive) {
      if (posting.isRaise)
        raiseCount += 1
//      if (!_posting.isCall && seat.putAmount > callAmount)
//        call = seat.putAmount
      pot add (player, diff, seat.isAllIn)
    }

    posting
  }

}
