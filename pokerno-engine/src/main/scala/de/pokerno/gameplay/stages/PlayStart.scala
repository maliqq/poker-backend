package de.pokerno.gameplay.stages

import de.pokerno.gameplay.{Events, Stage, stg}
import de.pokerno.util.Colored._

private[gameplay] case class PlayStart(ctx: stg.Context) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = {
    table.sitting.foreach { seat =>
      if (seat.isAllIn) {
        seat.taken()
        balance.available(seat.player).onSuccess { amount =>
          events broadcast Events.requireBuyIn(seat, stake, amount)
        }
      }
      if (seat.canPlay) {
        seat.playing()
        play.sit(seat)
      }
    }
    
    if (table.sitting.count(_.canPlay) <= 1) {
      throw Stage.Exit
    }
    
    events broadcast Events.playStart(ctx.gameplay)
  }

}
