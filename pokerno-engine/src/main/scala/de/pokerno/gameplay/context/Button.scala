package de.pokerno.gameplay.context

import de.pokerno.gameplay.ContextLike
import de.pokerno.gameplay.Events

private[gameplay] trait Button { g: ContextLike ⇒
  
  def setButton(pos: Int) {
    table.button.current = round.acting.current = pos
    events.publish(Events.buttonChange(table.button)) { _.all() }
  }

  def moveButton() = round.seats find { case (seat, _) ⇒ seat.isActive } map {
    case (_, pos) ⇒
      setButton(pos)
  }

}
