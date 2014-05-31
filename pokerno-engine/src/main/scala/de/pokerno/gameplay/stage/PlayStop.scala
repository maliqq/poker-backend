package de.pokerno.gameplay.stage

import de.pokerno.gameplay.{Event, Stage, StageContext}

case class PlayStop(ctx: StageContext) extends Stage {
  
  def apply() = {
    ctx broadcast Event.playStop()
    //play.finished()
  }

}
