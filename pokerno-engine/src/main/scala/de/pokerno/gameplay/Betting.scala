package de.pokerno.gameplay

import akka.actor.{ Actor, ActorRef, Cancellable }
import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import concurrent.duration._
import de.pokerno.gameplay.betting.NextTurn

case class Betting(ctx: stg.Context, betting: ActorRef) extends Bets with NextTurn {
  
  import ctx.gameplay._
  
  var timer: Cancellable = null
  
  // turn on big bets
  def bigBets() {
    round.bigBets = true
  }
  
  // add bet
  def add(player: Player, bet: Bet) {
    val pos = round.current
    val seat = table.seats(pos)
    if (seat.player == player) {
      if (timer != null) timer.cancel()
      Console printf("[betting] add {}", bet)
      addBet(bet)
      // next turn
      val turn = nextTurn() match {
          case Left(pos) =>
            
            requireBet(pos)
            
            Betting.StartTimer(30 seconds)

          case Right(None) =>       Betting.Stop
          case Right(Some(true)) => Betting.Showdown
          case _ =>                 Betting.Done
        }
      Console printf("[betting] next turn {}", turn)
      ctx.ref ! turn
    } else
      Console printf("[betting] not a turn of {}; current acting is {}", player, seat.player)
  }
  
  // timeout bet
  def timeout() {
    val pos = round.current
    val seat = table.seats(pos)
    
    val bet: Bet = seat.state match {
      case Seat.State.Away ⇒
        // force fold
        Bet.fold//(timeout = true)

      case _ ⇒
        // force check/fold
        if (round.call == 0 || seat.didCall(round.call))
          Bet.check//(timeout = true)
        else Bet.fold//(timeout = true)
    }

    Console printf("[betting] timeout")
    addBetWithTimeout(bet)
  }
  
}

object Betting {

  // start new round
  case object Start
  // require bet
  ////case class Require(amount: Decimal, limit: Game.Limit)
  // force bet
  case class Force(amount: Decimal)
  
  case class Add(player: Player, bet: Bet)

  trait Transition
  case object Next extends Transition
  // stop current deal
  case object Stop extends Transition
  // go to showdown
  case object Showdown extends Transition
  // betting done - wait for next street to occur
  case object Done extends Transition
  // require bet from this potision
  case class Require(pos: Int) extends Transition
  // start timer
  case class StartTimer(duration: FiniteDuration)
  // betting timeout - go to next seat
  case object Timeout
  // turn on big bet mode
  case object BigBets

}
