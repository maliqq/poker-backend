package de.pokerno.gameplay.context

import de.pokerno.gameplay.ContextLike
import de.pokerno.gameplay.Events

private[gameplay] trait Button { g: ContextLike ⇒
  
  def setButton(pos: Int) {
    table.button = round.current = pos
    events.broadcast(Events.buttonChange(table.button))
  }

  def moveButton() = round.seats find(_.isActive) map { seat =>
    setButton(seat.pos)
  }

}
