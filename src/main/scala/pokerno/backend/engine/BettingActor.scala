package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import scala.math.{ BigDecimal ⇒ Decimal }
import akka.actor.{ Actor, ActorRef, ActorLogging }

class BettingActor(gameplay: Gameplay) extends Actor with ActorLogging {
  import context._

  def round = gameplay.round

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
          if (!seat.isCalled(round.call)) seat check
      }

      if (round.seats.where(_ inPot).size < 2)
        self ! Betting.Stop
      else {
        val active = round.seats where (_ isPlaying)
        if (active.size == 0)
          self ! Betting.Done
        else {
          round requireBet(active.head)
        }
      }

    case Betting.Stop ⇒
      gameplay.showdown
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
