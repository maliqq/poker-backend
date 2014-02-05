package de.pokerno.gameplay

private[gameplay] trait Seating {
  ctx: ContextLike =>
    
    def prepareSeats(ctx: StageContext) {
      table.seatsAsList.filter (_ isReady) map (_ play)
    }
    
}
