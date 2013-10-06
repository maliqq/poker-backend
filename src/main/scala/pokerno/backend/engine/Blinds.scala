package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

trait Blinds {
  g: Gameplay ⇒
  def postBlinds {
    moveButton

    val round = table.seatsFromButton
    
    val active = round where (_ isActive)
    val waiting = round where (_ isWaitingBB)

    if (active.size + waiting.size < 2)
      return

    val List(sb, bb, _*) = active
    
    betting current = sb
    forceBet(Bet.SmallBlind)

    betting current = bb
    forceBet(Bet.BigBlind)
  }
}
