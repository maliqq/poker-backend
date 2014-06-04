package de.pokerno.gameplay.stages

import de.pokerno.gameplay.{Events, Stage, stg}

case class RotateGame(ctx: stg.Context) extends Stage {
  
  import ctx.gameplay._
  
  def apply() =
    if (variation.isMixed && gameRotation.hasNext) {
      game = gameRotation.next
      
      ctx broadcast Events.gameChange(game)
    }

}
