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
  def addBet(bet: Bet, timeout: Option[Boolean] = None) {
    val (seat, posted) = round.addBet(bet)
    events broadcast Events.addBet(seat, posted, timeout)
  }

  // force bet
  def forceBet(seat: Seat, betType: Bet.ForcedType) {
    val posted = round.forceBet(seat, betType)
    events broadcast Events.addBet(seat, posted)
  }

  // current betting round finished
  def doneBets() {
    events broadcast Events.declarePot(round.pot)
    round.complete()
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
  case class StartTimer(duration: FiniteDuration) extends Transition
  // betting timeout - go to next seat
  case object Timeout
  // turn on big bet mode
  case object BigBets

}
