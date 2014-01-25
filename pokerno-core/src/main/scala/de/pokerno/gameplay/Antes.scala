package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.ActorRef

/*
 * Стадия принудительных ставок-анте
 */
trait Antes {
  g: GameplayLike ⇒

  def postAntes(betting: ActorRef) = if (stake.ante.isDefined) {
    round.seats filter (_._1 isActive) foreach (round.forceBet(_, Bet.Ante))
    round.complete
  }
}
