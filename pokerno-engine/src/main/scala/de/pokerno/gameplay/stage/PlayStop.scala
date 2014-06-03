package de.pokerno.gameplay.stage

import de.pokerno.gameplay.{Events, Stage, StageContext}

case class PlayStop(ctx: StageContext) extends Stage {
  
  def apply() = {
    ctx broadcast Events.playStop()
    ctx.gameplay.play.end()
  }

}
