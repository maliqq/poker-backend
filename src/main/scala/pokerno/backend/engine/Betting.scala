package pokerno.backend.engine

import scala.math.{BigDecimal => Decimal}
import pokerno.backend.model._
import pokerno.backend.protocol._

object Betting extends Stage {
  class Context extends Round[Tuple2[Seat, Int]] {
    final val MaxRaiseCount = 8
    private var _raiseCount: Int = 0
    
    private var _bigBets: Boolean = false
    def bigBets = _bigBets
    def turnOnBigBets = {
      _bigBets = true
    }
    
    var pot: Pot = new Pot()
    private var _require: Bet.Requirement = new Bet.Requirement
    
    def pos: Int = {
      val (_, _pos) = current
      _pos
    }
    def seat: Seat = {
      val (_seat, _) = current
      _seat
    }
    
    def reset {
      _raiseCount = 0
      _require.reset
      stop
    }

    def force(bet: Bet) {
      _require.call = bet.amount
      add(bet)
    }
    
    def raiseRange(limit: Game.Limit, stake: Stake): Range = {
      val bb = stake.bigBlind
      
      limit match {
      case Game.NoLimit => Range(bb, seat.amount.get)
      case Game.PotLimit => Range(bb, pot.total)
      case Game.FixedLimit => if (_bigBets) Range(bb * 2, bb * 2) else Range(bb, bb)
      }
    }
    
    def require(r: Range) = {
      if (_raiseCount > MaxRaiseCount)
        _require.disableRaise
      else
        _require.adjustRaise(r, seat.amount.get)
      (_require.call.get, _require.raise.get.min, _require.raise.get.max)
    }
    
    def called(seat: Seat): Boolean = seat.called(_require.call.getOrElse(.0)) 
 
    def add(bet: Bet) {
      val (seat, pos) = current
      try {
        _require.validate(bet, seat)

        val amount = bet.amount
        val put = seat.bet(bet)
        if (amount > 0) {
          if (bet.betType != Bet.Call) {
            _require.call = amount
          }
    
          if (bet.betType == Bet.Raise) {
            _raiseCount += 1
          }
    
          pot.add(seat.player.get, put, seat.state == Seat.AllIn)
        }
      } catch {
      case e: Error => seat.fold
      }
    }
  }
  
  def apply(bigBets: Boolean): Stage = new Betting(bigBets)
  
  def run(context: Context) {
    //(new Betting).run(context)
  }
}

class Betting(private var _bigBets: Boolean = false) extends Stage {
  def context: Gameplay.Context
  def betting: Betting.Context
  def exit
  def run(context: Context) {
  }
  
  def requireBetting {
    val ring = context.table.ring
  
    ring.stillInPlay foreach { case (seat, pos) =>
      if (!betting.called(seat))
        seat.state = Seat.Play
    }
    
    if (ring.stillInPot.size < 2)
      return exit
    
    val active = ring.playing
    if (active.size == 0)
      return exit
  
    betting.start(active)
    
    val range = betting.raiseRange(context.game.limit, context.stake)
    val (call, min, max) = betting.require(range)
    val (seat, pos) = betting.current
    
    context.broadcast.one(seat.player.get) {
      new Message.RequireBet(call = call, min = min, max = max, pos = pos)
    }
  }
  
  def completeBetting {
    betting.reset
    
    val ring = context.table.ring
    ring.stillInPlay map(_._1.play)
  
    val total = betting.pot.total
    val message = new Message.CollectPot(total = total)
    context.broadcast.all(message)
  }

}
