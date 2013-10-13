package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

trait Blinds {
  g: Gameplay ⇒
  def postBlinds = if (game.options.hasBlinds) {
    moveButton
    
    val seats = round seats
    val active = seats where (_ isActive)
    val waiting = seats where (_ isWaitingBB)

    if (active.size + waiting.size < 2) {
      //
    } else {
      val List(sb, bb, _*) = active
      
      forceBet(sb, Bet.SmallBlind)
      forceBet(bb, Bet.BigBlind)
    }
  }
}
