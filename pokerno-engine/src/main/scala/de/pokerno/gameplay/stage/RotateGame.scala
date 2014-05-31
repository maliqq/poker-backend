package de.pokerno.gameplay.stage

import de.pokerno.gameplay.{Event, Stage, StageContext}

case class RotateGame(ctx: StageContext) extends Stage {
  
  import ctx.gameplay._
  
  def apply() =
    if (variation.isMixed && gameRotation.hasNext) {
      game = gameRotation.next
      
      ctx broadcast Event.gameChange(game)
    }

}
