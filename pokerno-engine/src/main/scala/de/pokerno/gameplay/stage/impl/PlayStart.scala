package de.pokerno.gameplay.stage.impl

import de.pokerno.gameplay.{Events, Stage}
import de.pokerno.util.Colored._

private[gameplay] case class PlayStart(ctx: Stage.Context) extends Stage {
  
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
