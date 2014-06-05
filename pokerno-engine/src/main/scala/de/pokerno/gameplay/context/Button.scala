package de.pokerno.gameplay.context

import de.pokerno.gameplay.ContextLike
import de.pokerno.gameplay.Events

private[gameplay] trait Button { g: ContextLike ⇒
  
  def setButton(pos: Int) {
    table.button = round.current = pos
    events.broadcast(Events.buttonChange(table.button))
  }

  def moveButton() = round.seats find { case (seat, _) ⇒ seat.isActive } map {
    case (_, pos) ⇒
      setButton(pos)
  }

}
