package de.pokerno.gameplay.stages

import de.pokerno.gameplay.{Events, Stage, stg}

case class PlayStop(ctx: stg.Context) extends Stage {
  
  def apply() = {
    ctx broadcast Events.playStop()
    ctx.gameplay.play.end()
  }

}
