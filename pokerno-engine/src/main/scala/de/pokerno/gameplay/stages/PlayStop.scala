package de.pokerno.gameplay.stages

import de.pokerno.gameplay.{Events, Stage, stg}

case class PlayStop(ctx: stg.Context) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = {
    play.end()
    table.playStop()
    events broadcast Events.playStop()
  }

}
