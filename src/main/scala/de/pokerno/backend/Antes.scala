package de.pokerno.backend

import de.pokerno.model._
import akka.actor.ActorRef

trait Antes {
  g: GameplayLike ⇒

  def postAntes(betting: ActorRef) = if (stake.ante.isDefined) {
    round.seats where (_ isActive) foreach (round.forceBet(_, Bet.Ante))
    round.complete
  }
}
