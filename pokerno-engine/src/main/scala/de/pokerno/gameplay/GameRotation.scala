package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.GameEvent
import de.pokerno.protocol.{ msg ⇒ message }

private[gameplay] trait GameRotation { g: ContextLike ⇒

  private lazy val gameRotation = new Rotation(variation.asInstanceOf[Mix].games)
  
  def rotateGame() = if (variation.isMixed && gameRotation.hasNext) {
    game = gameRotation.next
    events.publish(GameEvent.gameChange(game)) { _.all() }
  }

}
