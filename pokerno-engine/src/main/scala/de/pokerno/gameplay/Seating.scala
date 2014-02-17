package de.pokerno.gameplay

import de.pokerno.model.{ Player, Seat }

private[gameplay] trait Seating {
  ctx: ContextLike ⇒

  def prepareSeats(ctx: StageContext) {

    table.seatsAsList.foreach { seat ⇒
      if (seat.canPlayNextDeal) seat.play()
      if (seat.isAllIn) seat.idle()
    }

  }

}
