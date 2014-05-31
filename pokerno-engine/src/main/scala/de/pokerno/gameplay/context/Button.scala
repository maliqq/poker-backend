package de.pokerno.gameplay.context

import de.pokerno.gameplay.ContextLike
import de.pokerno.gameplay.Event
import de.pokerno.model.Ring.current2Int

private[gameplay] trait Button { g: ContextLike ⇒
  
  def setButton(pos: Int) {
    table.button.current = round.current = pos
    events.publish(Event.buttonChange(table.button)) { _.all() }
  }

  def moveButton() = round.seats find { case (seat, _) ⇒ seat.isActive } map {
    case (_, pos) ⇒
      setButton(pos)
  }

}
