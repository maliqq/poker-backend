package de.pokerno.gameplay.stage

import de.pokerno.gameplay.{Event, Stage, StageContext}

case class PlayStart(ctx: StageContext) extends Stage {
  
  def apply() = {
    ctx broadcast Event.playStart()
    //play.started() // FIXME ugly
  }

}