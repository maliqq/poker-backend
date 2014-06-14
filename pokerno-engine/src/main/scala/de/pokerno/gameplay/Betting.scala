package de.pokerno.gameplay

import akka.actor.{ Actor, ActorRef, Cancellable }
import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import concurrent.duration._
import de.pokerno.gameplay.betting.NextTurn

trait Betting {
  val gameplay: Context
  
  import gameplay._
  
  def round: betting.Round
  
  def nextTurn() = {
    NextTurn.decide(round.seats.filter(_.inPot), round.callAmount)
  }
  
  // require bet
  def requireBet(sitting: seat.Sitting) {
    round.requireBet(sitting)
    round.acting map { seat =>
      events broadcast Events.requireBet(seat)
    }
  }

  // add bet
  def addBet(sitting: seat.Sitting, bet: Bet, timeout: Boolean = false, forced: Boolean = false) {
    val posted = round.addBet(sitting, bet)
    val _timeout = if (timeout) Some(true) else None
    play.action(sitting.player, posted)
    events broadcast Events.addBet(sitting, posted, _timeout)
  }

  // force bet
  def forceBet(sitting: seat.Sitting, betType: BetType.Forced) {
    val posted = round.forceBet(sitting, betType)
    play.action(sitting.player, posted)
    events broadcast Events.addBet(sitting, posted)
  }

  // current betting round finished
  def complete() {
    round.reset()
    table.roundComplete()
    events broadcast Events.declarePot(round.pot)
  }
}

object Betting {

  // start new round
  case object Start
  
  // force bet
  case class Force(amount: Decimal)
  // add bet
  case class Add(player: Player, bet: Bet)
  // eject player from betting round
  case class Cancel(player: Player)

  trait Transition
  // require bet from this position
  case class Require(sitting: seat.Sitting) extends Transition
  // stop current deal
  case object Stop extends Transition
  // go to showdown
  case object Showdown extends Transition
  // betting done - wait for next street to occur
  case object Done extends Transition

  // betting timeout - go to next seat
  case object Timeout
  // turn on big bet mode
  case object BigBets

}
