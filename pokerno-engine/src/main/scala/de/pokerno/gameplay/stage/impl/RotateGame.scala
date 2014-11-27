package de.pokerno.gameplay.stage.impl

import de.pokerno.gameplay.{Events, Stage}

private[gameplay] case class RotateGame(ctx: Stage.Context) extends Stage {
  
  import ctx.gameplay._
  
  def apply() =
    if (variation.isMixed && gameRotation.hasNext) {
      game = gameRotation.next
      
      events broadcast Events.gameChange(game)
    }

}
