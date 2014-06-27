package de.pokerno.gameplay.stages

import de.pokerno.gameplay.{Events, Stage, stg}
import de.pokerno.util.Colored._

private[gameplay] case class PlayStart(ctx: stg.Context) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = {
    table.sitting.foreach { seat =>
      seat.clearCards()
      if (seat.canPlay) {
        seat.playing()
        play.sit(seat)
      }
    }
    
    events broadcast Events.playStart(ctx.gameplay)
  }

}
