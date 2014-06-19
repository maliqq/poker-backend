package de.pokerno.gameplay.betting

import de.pokerno.model._
import de.pokerno.gameplay.{Context => Gameplay, Round => GameplayRound}
import de.pokerno.util.Colored._
import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonInclude, JsonGetter}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Round(_table: Table, game: Game, stake: Stake) extends GameplayRound(_table) {
  protected override def acting_=(sitting: seat.Sitting) {
    acting.map(_.notBetting())
    super.acting = sitting
  }
  
  override def reset() {
    super.reset()
    raiseCount = 0
    _cachedCallAmount = 0
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
    
    sitting.call = amount
    acting = sitting

    val stack = sitting.stackAmount
    val amt = List(stack, amount) min
    val bet = betType(amt)

    addBet(sitting, bet)
  }

  def requireBet(sitting: seat.Sitting) = {
    // current amount to call
    val amount = _cachedCallAmount//_seats.filter(_.inPot).map(_.putAmount).max
    
    sitting.call = amount
    acting = sitting
    
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
    sitting.actingTimer.cancel()
    
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
