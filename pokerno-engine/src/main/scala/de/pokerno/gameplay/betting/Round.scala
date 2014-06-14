package de.pokerno.gameplay.betting

import org.slf4j.LoggerFactory
import de.pokerno.model._
import de.pokerno.gameplay.{Context => Gameplay}
import de.pokerno.util.Colored._
import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonInclude, JsonGetter}
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import math.{ BigDecimal ⇒ Decimal }

class Sitting2Acting extends com.fasterxml.jackson.databind.util.StdConverter[Option[seat.Sitting], Option[seat.Acting]] {
  override def convert(sitting: Option[seat.Sitting]): Option[seat.Acting] = sitting.map(_.asActing)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Round(@JsonIgnore table: Table, game: Game, stake: Stake) {
  private val log = LoggerFactory.getLogger(getClass)
  
  private var _seats = table.sittingFromButton
  def seats = _seats
  
  private var _acting: Option[seat.Sitting] = None
  @JsonSerialize(converter = classOf[Sitting2Acting]) def acting = _acting
  
  @JsonGetter def current = acting map(_.pos)
  
  private def acting(sitting: seat.Sitting, call: Decimal) {
    _acting.map(_.notActing())
    _seats = table.sittingFrom(sitting.pos)
    _acting = Some(sitting)
    sitting.call = call
  }
  
  def reset() {
    raiseCount = 0
    _cachedCallAmount = 0
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
  
  private var _cachedCallAmount: Decimal = 0
  def callAmount = _cachedCallAmount
  
  def forceBet(sitting: seat.Sitting, betType: BetType.Forced): Bet = {
    val amount = stake amount betType
    
    _cachedCallAmount = amount
    
    acting(sitting, amount)

    val stack = sitting.stackAmount
    val amt = List(stack, amount) min
    val bet = betType(amt)

    addBet(sitting, bet)
  }

  def requireBet(sitting: seat.Sitting) = {
    // current amount to call
    val amount = _cachedCallAmount//_seats.filter(_.inPot).map(_.putAmount).max
    
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
        warn("bet %s is not valid; seat=%s\n", _posting, sitting)
        Bet.fold
      }
    }

    val diff = sitting postBet posting

    if (posting.isActive) {
      if (posting.isRaise)
        raiseCount += 1
      if (!posting.isCall && sitting.putAmount > _cachedCallAmount)
        _cachedCallAmount = sitting.putAmount
      pot add (player, diff, sitting.isAllIn)
    }

    posting
  }

}
