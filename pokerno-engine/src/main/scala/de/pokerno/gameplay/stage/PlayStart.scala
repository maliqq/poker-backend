package de.pokerno.gameplay.stage

import de.pokerno.gameplay.{Events, Stage, StageContext}

case class PlayStart(ctx: StageContext) extends Stage {
  
  def apply() = {
    ctx broadcast Events.playStart()
  }

}