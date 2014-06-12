package de.pokerno.gameplay

import akka.actor.{ Actor, ActorRef, Cancellable }
import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import concurrent.duration._
import de.pokerno.gameplay.betting.NextTurn

trait Betting {
  val gameplay: Context
  
  import gameplay._
  
  // require bet
  def requireBet(seat: Seat) {
    round.requireBet(seat)
    round.acting map { seat =>
      events broadcast Events.requireBet(seat)
    }
  }

  // add bet
  def addBet(seat: Seat, bet: Bet, timeout: Boolean = false, forced: Boolean = false) {
    val posted = round.addBet(seat, bet)
    val _timeout = if (timeout) Some(true) else None
    events broadcast Events.addBet(seat, posted, _timeout)
  }

  // force bet
  def forceBet(seat: Seat, betType: Bet.ForcedType) {
    val posted = round.forceBet(seat, betType)
    events broadcast Events.addBet(seat, posted)
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
  case object Next extends Transition
  // stop current deal
  case object Stop extends Transition
  // go to showdown
  case object Showdown extends Transition
  // betting done - wait for next street to occur
  case object Done extends Transition
  // require bet from this potision
  case class Require(seat: Seat) extends Transition

  // betting timeout - go to next seat
  case object Timeout
  // turn on big bet mode
  case object BigBets

}
