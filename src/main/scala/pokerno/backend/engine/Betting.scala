package pokerno.backend.engine

import scala.math.{BigDecimal => Decimal}
import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.Actor

object Betting extends Stage {
  class Context(val items: List[Tuple2[Seat, Int]]) extends Round[Tuple2[Seat, Int]] {
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
    
    def clear {
      _raiseCount = 0
      _require.reset
      reset
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
  
  //def apply(bigBets: Boolean): Stage = new Betting(bigBets)
  
  def run(context: Gameplay.Context) {
    //(new Betting).run(context)
  }
}

import akka.actor.ActorRef

class Betting(bettingProcess: ActorRef, private var _bigBets: Boolean = false) extends Stage {
  var context: Gameplay.Context = null
  
  def run(_context: Gameplay.Context) {
    context = _context
  }
  
  def requireBetting {
    val table = context.table
  
    table.where(_.inPlay) foreach { case (seat, pos) =>
      if (!context.betting.called(seat))
        seat.state = Seat.Play
    }
    
    if (table.where(_.inPot).size < 2)
      bettingProcess ! BettingProcess.Showdown
      return
    
    val active = table.where(_.isPlaying)
    if (active.size == 0)
      bettingProcess ! BettingProcess.Exit
      return
  
    context.betting = new Betting.Context(active)
    
    val range = context.betting.raiseRange(context.game.limit, context.stake)
    val (call, min, max) = context.betting.require(range)
    val (seat, pos) = context.betting.current
    
    context.broadcast.one(seat.player.get) {
      Message.RequireBet(call = call, min = min, max = max, pos = pos)
    }
  }
  
  def completeBetting {
    context.betting.clear
    
    context.table.where(_.inPlay) map(_._1.play)
  
    val total = context.betting.pot.total
    val message = Message.CollectPot(total = total)
    context.broadcast.all(message)
  }

}

object BettingProcess {
  case object Showdown
  case object Exit
}

class BettingProcess extends Actor {
  case class Run
  case class Stop
  case class Next
  
  def receive = {
    case Run =>
      
    case Stop =>
      context.stop(self)
      context.parent ! Deal.Stop
      
    case Next =>
      
  }
}
