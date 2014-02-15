package de.pokerno.gameplay

import de.pokerno.model.{Player, Seat}

private[gameplay] trait Seating {
  ctx: ContextLike ⇒

  def prepareSeats(ctx: StageContext) {
    table.seatsAsList.filter (_ isReady) map (_ play)
  }

}
