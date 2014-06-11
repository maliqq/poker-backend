package de.pokerno.gameplay.stages

import de.pokerno.gameplay.{Stage, stg}

case class PrepareSeats(ctx: stg.Context) extends Stage {
  import concurrent.duration._
  import ctx.gameplay._
  
  def apply() = {
    table.seats.zipWithIndex.foreach {
      case (seat, pos) ⇒
        seat.clearCards()
        if (seat.canPlayNextDeal)     seat.play()
        else if (seat.isAllIn)        seat.idle()
        
//        seat.lastSeenOnline map { date =>
//          if (date.before(new java.util.Date(System.currentTimeMillis - 10.minutes.toMillis))) {
//            seat.clear()
//          }
//        }
    }
    
    if (table.seats.count(_.canPlayNextDeal) <= 1) {
      throw Stage.Exit
    }
  }
  
}
