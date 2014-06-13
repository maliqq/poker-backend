package de.pokerno.gameplay.stages

import de.pokerno.gameplay.{Events, Stage, stg}
import de.pokerno.util.Colored._

case class PlayStart(ctx: stg.Context) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = {
    table.playStart()
    if (table.sitting.count(_.canPlay) <= 1) {
      throw Stage.Exit
    }
    
    events broadcast Events.playStart(ctx.gameplay)
  }

}
