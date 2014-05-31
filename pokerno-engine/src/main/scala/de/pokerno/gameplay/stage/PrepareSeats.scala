package de.pokerno.gameplay.stage

import de.pokerno.gameplay.{Stage, StageContext}

case class PrepareSeats(ctx: StageContext) extends Stage {
  import concurrent.duration._
  import ctx.gameplay._
  
  def apply() = {
    table.seats.zipWithIndex.foreach {
      case (seat, pos) ⇒
        if (seat.canPlayNextDeal)     seat.play()
        else if (seat.isAllIn)        seat.idle()
        
        if (seat.lastSeenOnlineBefore(System.currentTimeMillis() - 10.minutes.toMillis)) {
          table.clearSeat(pos)
        }
        seat.clearPut()
    }
    
    if (table.seats.count(_.canPlayNextDeal) <= 1) throw Stage.Exit
  }
  
}
