package pokerno.backend.engine

import scala.math.{ BigDecimal ⇒ Decimal }
import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.{ Actor, ActorLogging, ActorRef }

trait Betting {
g: Gameplay =>
  def self: ActorRef
  
  def forceFold = {}
  
  def bettingRound {
    val (seat, pos) = betting.current
    
    val round = table.seats from(pos)
    
    round where(_ inPlay) foreach {
      case (seat, pos) ⇒
        if (!betting.called(seat))
          seat.state = Seat.Play
    }

    if (round.where(_ inPot).size < 2) {
      self ! Betting.Stop
      return
    }

    val active = round where (_ isPlaying)
    if (active.size == 0) {
      self ! Betting.Done
      return
    }
    
    betting current = (seat, pos)

    requireBet
  }
  
}

object Betting {
  // go to next seat
  case object Next
  // stop current deal
  case object Stop
  // betting done - wait for next street to occur
  case object Done
  // betting timeout - go to next seat
  case object Timeout
}
