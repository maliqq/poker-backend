package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

object Gameplay {
  class Context(
    val dealer: Dealer,
    val broadcast: Broadcast,
    val game: Game,
    val stake: Stake,
    val table: Table
  ) {
    
    def moveButton {
      table.moveButton
      val message = new Message.MoveButton(pos = table.button)
    }
    
    def setButton(pos: Int) {
      table.button = pos
      val message = new Message.MoveButton(pos = table.button)
    }
    
  }
}

class Gameplay {
  
}
