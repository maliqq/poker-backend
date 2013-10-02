package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

trait Blinds {
gameplay: Gameplay =>
  def postBlinds {
    moveButton
    
    val active = table.where(_.isActive)
    val waiting = table.where(_.isWaitingBB)
    
    if (active.size + waiting.size < 2)
      return

    betting = new Betting.Context(active)
    
    forceBet(Bet.SmallBlind)
    betting.move
    
    forceBet(Bet.SmallBlind)
    betting.move
  }
}
