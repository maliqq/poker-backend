package de.pokerno.gameplay.stage

import de.pokerno.gameplay.{Events, Stage, StageContext}

case class RotateGame(ctx: StageContext) extends Stage {
  
  import ctx.gameplay._
  
  def apply() =
    if (variation.isMixed && gameRotation.hasNext) {
      game = gameRotation.next
      
      ctx broadcast Events.gameChange(game)
    }

}
