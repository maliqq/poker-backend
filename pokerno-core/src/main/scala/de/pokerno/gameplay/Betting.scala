package de.pokerno.gameplay

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import de.pokerno.protocol.{msg => message}
import de.pokerno.protocol.rpc
import akka.actor.Actor

object Betting {

  // start new round
  case object Start
  // require bet
  case class Require(amount: Decimal, limit: Game.Limit)
  // force bet
  case class Force(amount: Decimal)
  case class Add(bet: Bet)

  // go to next seat
  case object Next
  // stop current deal
  case object Stop
  // betting done - wait for next street to occur
  case object Done
  // betting timeout - go to next seat
  case object Timeout
  // turn on big bet mode
  case object BigBets

}

trait Betting {
  
  a: Actor =>
    
  def round: BettingRound

  def betting: Receive = {
    case Betting.Add(bet) ⇒
      round.addBet(bet)
      nextTurn

    case Betting.Start ⇒
      round.reset
      nextTurn

    case Betting.Next ⇒
      nextTurn
      
    case Betting.Stop ⇒
      parent ! Street.Exit
      stop(self)

    case Betting.Timeout ⇒
      self ! Betting.Next

    case Betting.BigBets ⇒
      round.bigBets = true

    case Betting.Done ⇒
      round.complete
      parent ! Street.Next
  }
  
  private def stop {
    self ! Betting.Stop
  } 
  
  private def nextTurn {
    round.move
    round.seats filter (_._1 inPlay) foreach {
      case (seat, pos) ⇒
        if (!seat.isCalled(round.call)) seat playing
    }

    if (round.seats.filter(_._1 inPot).size < 2)
      stop
    else {
      val active = round.seats filter (_._1 isPlaying)

      //Console printf("%sACTIVE:\n%s%s\n", Console.GREEN, round.seats.value.map(_._1.toString).toList.mkString("\n"), Console.RESET)

      if (active.size == 0)
        self ! Betting.Done
      else {
        round requireBet (active.head)
      }
    }
  }

}
