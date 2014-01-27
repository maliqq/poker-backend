package de.pokerno.gameplay

import de.pokerno.protocol.{msg => message}

trait Button {
  
  g: GameplayLike =>
    
  def setButton(pos: Int) {
    table.button.current = pos
    round.current = pos
    events.buttonChange(table.button)
  }
  
  def moveButton {
    table.button.move
    round.current = table.button
    events.buttonChange(table.button)
  }

}
