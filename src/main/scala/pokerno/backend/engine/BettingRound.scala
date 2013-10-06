package pokerno.backend.engine

import pokerno.backend.model._

class BettingRound(button: Tuple2[Seat, Int]) {
  final val MaxRaiseCount = 8
  private var _raiseCount: Int = 0

  private var _bigBets: Boolean = false
  def bigBets = _bigBets
  def turnOnBigBets = {
    _bigBets = true
  }

  var pot: Pot = new Pot()
  private var _require: Bet.Validation = new Bet.Validation

  private var _current: Tuple2[Seat, Int] = button
  def current = _current
  def current_=(item: Tuple2[Seat, Int]) {
    _current = item
  }
  
  def pos: Int = {
    val (_, _pos) = current
    _pos
  }
  def seat: Seat = {
    val (_seat, _) = current
    _seat
  }
  
  def clear {
    _raiseCount = 0
    _require.reset
    _current = button
  }

  def force(bet: Bet) {
    _require.call = bet.amount
    add(bet)
  }

  def require(r: Range) = {
    if (_raiseCount > MaxRaiseCount)
      _require disableRaise
    else
      _require adjustRaise (r, seat.amount get)
    (_require.call get, _require.raise.get min, _require.raise.get max)
  }
  
  def range(limit: Game.Limit, stake: Stake) = limit.raise(seat.amount get, stake.bigBlind, pot total)

  def called(seat: Seat): Boolean = seat.called(_require.call getOrElse (.0))

  def add(bet: Bet) {
    val (seat, pos) = current
    try {
      _require validate (bet, seat)

      val amount = bet.amount
      val put = seat.bet(bet)
      if (amount > 0) {
        if (bet.betType != Bet.Call) {
          _require.call = amount
        }

        if (bet.betType == Bet.Raise) {
          _raiseCount += 1
        }

        pot add (seat.player get, put, seat.state == Seat.AllIn)
      }
    } catch {
      case e: Error ⇒ seat fold
    }
  }
}
