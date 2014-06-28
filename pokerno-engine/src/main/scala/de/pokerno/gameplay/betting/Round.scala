package de.pokerno.gameplay.betting

import de.pokerno.model._
import de.pokerno.model.table.seat.Sitting
import de.pokerno.gameplay.{Context => Gameplay, Round => GameplayRound}
import de.pokerno.util.Colored._
import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonInclude, JsonGetter}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Round(_table: Table, game: Game, stake: Stake, play: Play) extends GameplayRound(_table) {
  protected override def acting_=(seat: Sitting) {
    acting.map(_.notBetting())
    super.acting = seat
  }
  
  import play.pot
  
  override def reset() {
    super.reset()
    raiseCount = 0
    _cachedCallAmount = 0
  }
  
  @JsonIgnore var bigBets: Boolean = false

  // limit number of raises per one street
  private final val MaxRaiseCount = 8
  private var raiseCount: Int = 0
  
  private var _cachedCallAmount: Decimal = 0
  def callAmount = _cachedCallAmount
  
  def forceBet(seat: Sitting, betType: BetType.Forced): Bet = {
    val amount = stake amount betType
    _cachedCallAmount = amount
    
    seat.call = amount
    acting = seat

    val stack = seat.stackAmount
    val amt = List(stack, amount) min
    val bet = betType(amt)

    addBet(seat, bet)
  }

  def requireBet(seat: Sitting) = {
    // current amount to call
    val amount = _cachedCallAmount//_seats.filter(_.inPot).map(_.putAmount).max
    
    seat.call = amount
    acting = seat
    
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
  
  def addBet(seat: Sitting, _bet: Bet): Bet = {
    seat.actingTimer.cancel()
    
    val player = seat.player
    
    val posting = {
      val _posting = seat.posting(_bet)
    
      if (seat.canBet(_posting, stake)) _posting
      else {
        warn("bet %s is not valid; seat=%s\n", _posting, seat)
        Bet.fold
      }
    }

    val diff = seat postBet posting

    if (posting.isActive) {
      if (posting.isRaise)
        raiseCount += 1
      if (!posting.isCall && seat.putAmount > _cachedCallAmount)
        _cachedCallAmount = seat.putAmount
      pot add (player, diff, seat.isAllIn)
      // if (seat.isAllIn) {
      //   posting.toActive.allIn()
      // }
    }

    posting
  }

}
