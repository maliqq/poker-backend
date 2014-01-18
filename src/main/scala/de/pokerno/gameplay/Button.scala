package de.pokerno.gameplay

import de.pokerno.protocol.{msg => message}

trait Button {
  
  g: GameplayLike =>
  
  def setButton(pos: Int) {
    table.button.current = pos
    round.current = pos
    events.publish(message.ButtonChange(_button = table.button))
  }
  
  def moveButton {
    table.button.move
    round.current = table.button
    events.publish(
        message.ButtonChange(_button = table.button)
      )
  }

}
