package de.pokerno.gameplay.stages

import de.pokerno.gameplay.{Events, Stage, stg}
import de.pokerno.util.Colored._

case class PlayStart(ctx: stg.Context) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = {
    info("BEFORE PREPARE:\n%s\n", table)
    table.playStart()
    info("AFTER PREPARE:\n%s\n", table)
    
    if (table.seats.count(_.canPlay) <= 1) {
      throw Stage.Exit
    }
    
    events broadcast Events.playStart(ctx.gameplay)
  }

}
