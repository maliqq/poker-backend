package de.pokerno.gameplay.stages

import de.pokerno.gameplay.{Events, Stage, stg}

case class PlayStart(ctx: stg.Context) extends Stage {
  
  def apply() = {
    ctx broadcast Events.playStart()
  }

}