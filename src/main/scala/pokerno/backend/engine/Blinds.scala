package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.ActorRef

trait Blinds {
  g: Gameplay ⇒
  def postBlinds(betting: ActorRef) = if (game.options.hasBlinds) {
    moveButton
    
    val seats = round seats
    val active = seats where (_ isActive)
    val waiting = seats where (_ isWaitingBB)

    if (active.size + waiting.size < 2) {
      //
    } else {
      val List(sb, bb, _*) = active
      
      Console printf("Blinds=%s\n", List(sb._2, bb._2))

      round.forceBet(sb, stake amount(Bet.SmallBlind))
      
      round.forceBet(bb, stake amount(Bet.BigBlind))
    }
  }
}
