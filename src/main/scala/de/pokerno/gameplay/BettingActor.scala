package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.{msg => message}
import de.pokerno.protocol.rpc
import math.{ BigDecimal ⇒ Decimal }
import akka.actor.{ Actor, ActorRef, ActorLogging }

class BettingActor(val round: BettingRound) extends Actor
                                               with ActorLogging {
  import context._

  def receive = {
    case rpc.AddBet(player, bet) ⇒
      round.addBet(bet)
      self ! Betting.Next

    case Betting.Start ⇒
      round.reset
      self ! Betting.Next

    case Betting.Next ⇒
      round.move
      round.seats filter (_._1 inPlay) foreach {
        case (seat, pos) ⇒
          if (!seat.isCalled(round.call)) seat playing
      }

      if (round.seats.filter(_._1 inPot).size < 2)
        self ! Betting.Stop
      else {
        val active = round.seats filter (_._1 isPlaying)

        //Console printf("%sACTIVE:\n%s%s\n", Console.GREEN, round.seats.value.map(_._1.toString).toList.mkString("\n"), Console.RESET)

        if (active.size == 0)
          self ! Betting.Done
        else {
          round requireBet (active.head)
        }
      }

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

}
