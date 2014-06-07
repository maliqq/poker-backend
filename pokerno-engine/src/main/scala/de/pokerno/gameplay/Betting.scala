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
  def requireBet(pos: Int) {
    val seat = round requireBet pos
    val player = seat.player.get
    events broadcast Events.requireBet(pos, player, round.call, round.raise)
  }

  // add bet
  def addBet(bet: Bet) {
    val (seat, posted) = round.addBet(bet)
    val pos = round.current
    val player = seat.player.get
    events broadcast Events.addBet(pos, player, posted)
  }
  
  def addBetWithTimeout(bet: Bet) {
    val (seat, posted) = round.addBet(bet)
    val pos = round.current
    val player = seat.player.get
    val event = Events.addBet(pos, player, posted)
    event.timeout = Some(true)
    events broadcast event
  }

  // force bet
  def forceBet(pos: Int, betType: Bet.ForcedType) {
    val (seat, posted) = round.forceBet(pos, betType)
    val player = seat.player.get
    events broadcast Events.addBet(pos, player, posted)
  }

  // current betting round finished
  def doneBets() {
    events broadcast Events.declarePot(round.pot.total,
        round.pot.sidePots.map(_.total))
    round complete()
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
