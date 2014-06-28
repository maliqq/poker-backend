package de.pokerno.gameplay.betting

import de.pokerno.model.{Player, Bet}
import de.pokerno.model.table.Seat
import de.pokerno.gameplay.round.{Context => RoundContext}
import de.pokerno.gameplay.{Betting, Context => Gameplay}
import de.pokerno.util.Colored._ 
import akka.actor.{ActorRef, Cancellable}
import concurrent.duration._

private[gameplay] class Context(_gameplay: Gameplay, ref: ActorRef) extends RoundContext(_gameplay) with Betting {
  
  import gameplay._
  
  override def round = bettingRound
  
  // turn on big bets
  def bigBets() {
    round.bigBets = true
  }
  
  // add bet
  def add(player: Player, bet: Bet): Unit =
    round.acting match {
      case Some(seat) if seat.player == player && !seat.isLeaving => // FIXME willLeave
        
        info("[betting] %s add %s", player, bet)
        addBet(seat, bet)
        // next turn
        ref ! nextTurn()
        
      case _ =>
        warn("[betting] add: not a turn of %s; current acting is %s", player, round.acting)
    }
  
  //
  def cancel(player: Player) = round.acting match {
    case Some(seat) if seat.player == player =>
      info("[betting] %s cancel", player)
      addBet(seat, Bet.fold, forced = true)
      ref ! nextTurn()
    case _ =>
  }
  
  // timeout bet
  def timeout() = round.acting match {
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
