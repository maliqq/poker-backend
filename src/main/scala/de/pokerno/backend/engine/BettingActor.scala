package de.pokerno.backend.engine

import de.pokerno.backend.model._
import de.pokerno.backend.protocol._
import scala.math.{ BigDecimal ⇒ Decimal }
import akka.actor.{ Actor, ActorRef, ActorLogging }

class BettingActor(val round: BettingRound) extends Actor with ActorLogging {
  import context._

  def receive = {
    case Message.AddBet(pos, bet) ⇒
      round.addBet(bet)
      self ! Betting.Next

    case Betting.Start ⇒
      round.reset
      self ! Betting.Next

    case Betting.Next ⇒
      round.move
      round.seats where (_ inPlay) foreach {
        case (seat, pos) ⇒
          if (!seat.isCalled(round.call)) seat playing
      }

      if (round.seats.where(_ inPot).size < 2)
        self ! Betting.Stop
      else {
        val active = round.seats where (_ isPlaying)

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
