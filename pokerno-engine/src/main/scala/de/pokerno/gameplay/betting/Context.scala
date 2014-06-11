package de.pokerno.gameplay.betting

import org.slf4j.LoggerFactory
import de.pokerno.model.{Player, Bet, Seat}
import akka.actor.{ActorRef, Cancellable}
import de.pokerno.gameplay.{Betting, Context => Gameplay}
import concurrent.duration._

class Context(val gameplay: Gameplay, ref: ActorRef) extends Betting with NextTurn {
  
  import gameplay._

  private val log = LoggerFactory.getLogger(getClass)
  
  var timer: Cancellable = null
  
  // turn on big bets
  def bigBets() {
    round.bigBets = true
  }
  
  // add bet
  def add(player: Player, bet: Bet): Unit =
    round.acting match {
      case Some(seat) =>
        seat.player match { case Some(p) if p == player =>
            
          if (timer != null) timer.cancel()
          log.info("[betting] add {}", bet)
          addBet(bet)
          // next turn
          ref ! nextTurn()
            
        case None =>
          log.error("[betting] add: round.acting.player == None")
            
        case _ =>
          log.warn(f"[betting] add: not a turn of $player; current acting is ${seat.player}")
        }
        
      case None =>
        log.error("[betting] add: round.acting == None")
    }
  
  // timeout bet
  def timeout(): Unit = round.acting match {
    case Some(seat) =>
      val bet: Bet = seat.state match {
        case Seat.State.Away ⇒
          // force fold
          Bet.fold
  
        case _ ⇒
          // force check/fold
          if (round.call == 0 || seat.didCall(round.callAmount))
            Bet.check
          else Bet.fold
      }
  
      addBet(bet, timeout = true)
      ref ! nextTurn()
    
    case None =>
      log.error("[betting] timeout: round.acting == None")
  }
  
}
