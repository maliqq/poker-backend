package de.pokerno.gameplay

import de.pokerno.model.{ Player, Seat }
import concurrent.duration._

private[gameplay] trait Seating {
  ctx: ContextLike ⇒

  def prepareSeats(ctx: StageContext) {

    table.seatsAsList.zipWithIndex.foreach { case (seat, pos) ⇒
      if (seat.canPlayNextDeal) seat.play()
      if (seat.isAllIn) seat.idle()
      if (seat.lastSeenOnlineBefore(System.currentTimeMillis() - 10.minutes.toMillis)) {
        table.clearSeat(pos)
      }
      seat.clearPut()
    }
    
  }

}
