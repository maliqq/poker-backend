package de.pokerno.gameplay.betting

import org.slf4j.LoggerFactory
import de.pokerno.model.{Player, Bet, Seat}
import de.pokerno.gameplay.{Betting, Context => Gameplay}
import de.pokerno.util.Colored._ 
import akka.actor.{ActorRef, Cancellable}
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
        if (seat.player == player) {
          
          if (timer != null) timer.cancel()
          info("[betting] add %s", bet)
          addBet(seat, bet)
          // next turn
          ref ! nextTurn()
            
        } else {
          warn("[betting] add: not a turn of %s; current acting is %s", player, seat)
        }
        
      case None =>
        error("[betting] add: round.acting == None")
    }
  
  //
  def cancel(player: Player): Unit = round.acting match {
    case Some(seat) =>
      if (seat.player == player) {
        // leaving currently acting player: just fold
        addBet(seat, Bet.fold, forced = true)
        ref ! nextTurn()
      } else {
        // in headsup - return uncalled bet, fold
        // in multipot - just leave orphan bet
      }
    case None =>
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
          if (round.callAmount == 0 || seat.isCalled(round.callAmount))
            Bet.check
          else Bet.fold
      }
  
      addBet(seat, bet, timeout = true)
      ref ! nextTurn()
    
    case None =>
      error("[betting] timeout: round.acting=%s", round.acting)
  }
  
}
