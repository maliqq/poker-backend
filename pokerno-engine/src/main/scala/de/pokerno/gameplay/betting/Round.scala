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
  
  private var _seats = table.sittingFromButton
  def seats = _seats
  
  @JsonProperty def current = acting map(_.pos)
  
  private var _acting: Option[seat.Sitting] = None
  @JsonSerialize(converter = classOf[Seat2Acting]) def acting = _acting
  
  private def acting(sitting: seat.Sitting, call: Decimal) {
    _acting.map(_.notActing())
    _seats = table.sittingFrom(sitting.pos)
    _acting = Some(sitting)
    sitting.call = call
  }
  
  def reset() {
    raiseCount = 0
    _acting = None
    _seats = table.sittingFromButton
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
  
  def forceBet(sitting: seat.Sitting, betType: BetType.Forced): Bet = {
    val amount = stake amount betType
    
    acting(sitting, amount)

    val stack = sitting.stack.get
    val amt = List(stack, amount) min
    val bet = betType(amt)

    addBet(sitting, bet)
  }

  def requireBet(sitting: seat.Sitting) = {
    val amount = callAmount
    
    acting(sitting, amount)
    
    val total = sitting.total
    if (total <= amount || raiseCount >= MaxRaiseCount)
      sitting.disableRaise()
    else {
      val limit = game.limit
      val blind = if (bigBets) stake.bigBlind * 2 else stake.bigBlind
      val (min, max) = limit raise (total, blind + amount, pot.total)
      sitting.raise = (List(total, min) min, List(total, max) min)
    }
  }
  
  def addBet(sitting: seat.Sitting, _bet: Bet): Bet = {
    val player = sitting.player

    val posting = {
      val _posting = sitting.posting(_bet)
    
      if (sitting.canBet(_posting, stake)) _posting
      else {
        warn("bet %s is not valid; call=%.2f seat=%s\n", _posting, callAmount, sitting)
        Bet.fold
      }
    }

    val diff = sitting postBet posting

    if (posting.isActive) {
      if (posting.isRaise)
        raiseCount += 1
//      if (!_posting.isCall && seat.putAmount > callAmount)
//        call = seat.putAmount
      pot add (player, diff, sitting.isAllIn)
    }

    posting
  }

}
