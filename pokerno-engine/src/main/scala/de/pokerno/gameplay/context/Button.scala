package de.pokerno.gameplay.context

import de.pokerno.gameplay.ContextLike
import de.pokerno.gameplay.Events

private[gameplay] trait Button { g: ContextLike ⇒
  
  def round = bettingRound
  
  def setButton(pos: Int) {
    table.button = pos
    
    round.reset()
    
    events broadcast Events.buttonChange(pos)
  }

  def moveButton() = round.seats find(_.isActive) map { seat =>
    setButton(seat.pos)
  }

}
