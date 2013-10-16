package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.ActorRef

trait Antes {
  g: Gameplay ⇒

  def postAntes(betting: ActorRef) = if (stake.ante.isDefined) {
    round.seats where (_ isActive) foreach (forceBet(betting, _, Bet.Ante))
    betting ! Betting.Done
  }
}
