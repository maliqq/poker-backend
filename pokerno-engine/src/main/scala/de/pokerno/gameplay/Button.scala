package de.pokerno.gameplay

import de.pokerno.protocol.{ msg ⇒ message }

private[gameplay] trait Button {

  g: ContextLike ⇒

  def setButton(pos: Int) {
    table.button.current = pos
    round.current = pos
    events.publish(
        Events.buttonChange(table.button)
      )
  }

  def moveButton {
    round.seats.find {
      case (seat, pos) ⇒
        seat.isActive
    } map {
      case (_, pos) ⇒
        setButton(pos)
    }
  }

}
