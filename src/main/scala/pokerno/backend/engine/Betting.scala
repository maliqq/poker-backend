package pokerno.backend.engine

import scala.math.{BigDecimal => Decimal}
import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.{Actor, ActorLogging, ActorRef}

object Betting {
  // go to next seat
  case object Next
  // stop current deal
  case object Stop
  // betting done - wait for next street to occur
  case object Done
  // betting timeout - go to next seat
  case object Timeout
  
  class Process(gameplay: Gameplay) extends Actor with ActorLogging {
    import context._
    var betting: Betting = null
    
    override def preStart {
      betting = new Betting(gameplay, self)
    }
    
    def receive = {
      case Next =>
        log.info("require bet")
        betting.require
      
      case Done =>
        log.info("done betting round")
        stop(self)
        
      case Stop =>
        log.info("go to showdown")
        stop(self)
        parent ! Deal.Stop

      case Timeout =>
        log.info("timeout")
        betting.forceFold
        self ! Next
    }
    
    override def postStop {
      log.info("betting complete")
      betting.complete
    }
  }

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
}

import akka.actor.ActorRef

class Betting(val gameplay: Gameplay, actor: ActorRef = null) {
  private var _bigBets: Boolean = false
  
  def forceFold {
    
  }
  
  def require {
    gameplay.table.where(_.inPlay) foreach { case (seat, pos) =>
      if (!gameplay.betting.called(seat))
        seat.state = Seat.Play
    }
    
    if (gameplay.table.where(_.inPot).size < 2)
      actor ! Betting.Stop
      return
    
    val active = gameplay.table.where(_.isPlaying)
    if (active.size == 0)
      actor ! Betting.Done
      return
  
    gameplay.betting = new Betting.Context(active)
    
    val range = gameplay.betting.raiseRange(gameplay.game.limit, gameplay.stake)
    val (call, min, max) = gameplay.betting.require(range)
    val (seat, pos) = gameplay.betting.current
    
    gameplay.broadcast.one(seat.player.get) {
      Message.RequireBet(call = call, min = min, max = max, pos = pos)
    }
  }
  
  def complete {
    gameplay.betting.clear
    
    gameplay.table.where(_.inPlay) map(_._1.play)
  
    val total = gameplay.betting.pot.total
    val message = Message.CollectPot(total = total)
    gameplay.broadcast.all(message)
  }

}
