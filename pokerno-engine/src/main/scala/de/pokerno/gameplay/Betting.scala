package de.pokerno.gameplay

import akka.actor.{ Actor, ActorRef, Cancellable }
import de.pokerno.model._
import de.pokerno.model.table.seat.Sitting
import concurrent.duration._
import de.pokerno.gameplay.betting.NextTurn

private[gameplay] trait Betting {
  val gameplay: Context
  
  import gameplay._
  
  def round: betting.Round
  
  def nextTurn() = {
    NextTurn.decide(round.seats.filter(_.inPot), round.callAmount)
  }
  
  // require bet
  def require(seat: Sitting) {
    round.requireBet(seat)
    
    if (seat.willLeave) {
      seat.autoplay.checkFolds
    }
    
    if (!seat.autoplay.isDefined || !autoplay(seat)) {
      events broadcast Events.requireBet(seat)
    }
  }
  
  def autoplay(seat: Sitting): Boolean = {
    val bet = if (seat.callAmount > 0) {
      if (seat.autoplay.willCallAny)
        Some(Bet.call(seat.callAmount))
      else if (seat.autoplay.willFold || seat.autoplay.willCheckFold)
        Some(Bet.fold)
      else
        None
    } else Some(Bet.check)
    
    bet.map { _bet =>
      addBet(seat, _bet, forced = true)
      true
    }.getOrElse(false)
  }

  // add bet
  def addBet(seat: Sitting, bet: Bet, timeout: Boolean = false, forced: Boolean = false) {
    val posted = round.addBet(seat, bet)
    val _timeout = if (timeout) Some(true) else None
    val _allIn = if (seat.isAllIn) Some(true) else None
    play.action(Action(seat.player, posted, isTimeout = _timeout, isAllIn = _allIn))
    events broadcast Events.addBet(seat, posted, _timeout)
  }

  // force bet
  def forceBet(seat: Sitting, betType: BetType.Forced) {
    val posted = round.forceBet(seat, betType)
    play.action(Action(seat.player, posted, isForced = Some(true)))
    events broadcast Events.addBet(seat, posted)
  }

  // current betting round finished
  def complete() {
    round.reset()
    play.pot.complete()
    // prepare seats for next round or showdown
    table.sitting.foreach { seat =>
      seat.clearAction()
      if (seat.inPot) seat.playing()
    }
    events broadcast Events.declarePot(play.pot)
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

  // go to showdown
  case object Showdown extends Round.Transition

  // turn on big bet mode
  case object BigBets

}
