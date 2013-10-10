package pokerno.backend.engine

import pokerno.backend.model._

class BettingRound(button: Tuple2[Seat, Int]) {
  final val MaxRaiseCount = 8
  private var raiseCount: Int = 0

  var bigBets: Boolean = false
  var pot: Pot = new Pot()
  
  private var required: Bet.Validation = new Bet.Validation

  var current: Tuple2[Seat, Int] = button
  def pos: Int = current._2
  def seat: Seat = current._1

  def clear {
    raiseCount = 0
    required.reset
    current = button
  }

  def force(bet: Bet) {
    required.call = bet.amount
    
    this += bet
  }

  def require(r: Range) = {
    if (raiseCount > MaxRaiseCount)
      required disableRaise
    else
      required adjustRaise (r, seat.amount get)
    (required.call get, required.raise.get min, required.raise.get max)
  }

  def range(limit: Game.Limit, stake: Stake) = limit.raise(seat.amount get, stake.bigBlind, pot total)

  def called(seat: Seat): Boolean = seat.called(required.call getOrElse (.0))

  def +=(bet: Bet) = try {
    required validate (bet, seat)

    val amount = bet.amount
    val put = seat.bet(bet)
    if (amount > 0) {
      if (bet.betType != Bet.Call) {
        required.call = amount
      }

      if (bet.betType == Bet.Raise) {
        raiseCount += 1
      }

      pot add (seat.player get, put, seat.state == Seat.AllIn)
    }
  } catch {
    case e: Error ⇒ seat fold
  }
}
