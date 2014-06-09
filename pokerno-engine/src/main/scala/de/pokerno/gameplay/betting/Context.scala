package de.pokerno.gameplay.betting

import org.slf4j.LoggerFactory
import de.pokerno.model.{Player, Bet, Seat}
import akka.actor.{ActorRef, Cancellable}
import de.pokerno.gameplay.{Betting, Context => Gameplay}
import concurrent.duration._

class Context(val gameplay: Gameplay, ref: ActorRef) extends Betting with NextTurn {
  
  import gameplay._

  val log = LoggerFactory.getLogger(getClass)
  
  var timer: Cancellable = null
  
  // turn on big bets
  def bigBets() {
    round.bigBets = true
  }
  
  def decideNextTurn(): Betting.Transition = nextTurn() match {
    case Left(pos) =>
      requireBet(pos)
      Betting.StartTimer(15 seconds)

    case Right(None) =>       Betting.Stop
    case Right(Some(true)) => Betting.Showdown
    case _ =>                 Betting.Done
  }
  
  // add bet
  def add(player: Player, bet: Bet) {
    val pos = round.current
    val seat = table.seats(pos)
    if (seat.player.get == player) { // FIXME: player.get
      if (timer != null) timer.cancel()
      log.info("[betting] add {}", bet)
      addBet(bet)
      // next turn
      ref ! decideNextTurn()
    } else
      log.warn(f"[betting] not a turn of $player; current acting is ${seat.player}")
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
        if (round.call == 0 || seat.didCall(round.callAmount))
          Bet.check//(timeout = true)
        else Bet.fold//(timeout = true)
    }

    addBetWithTimeout(bet)
  }
  
}
