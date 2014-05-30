package de.pokerno.gameplay

import de.pokerno.protocol.{ msg ⇒ message }
import de.pokerno.protocol.GameEvent

private[gameplay] trait Button {

  g: ContextLike ⇒
  
  def setButton(pos: Int) {
    table.button.current = round.current = pos
    events.publish(GameEvent.buttonChange(table.button)) { _.all() }
  }

  def moveButton = round.seats find { case (seat, _) ⇒ seat.isActive } map {
    case (_, pos) ⇒
      setButton(pos)
  }

}
