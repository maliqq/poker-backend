package de.pokerno.gameplay

import de.pokerno.model._

object Gameplay {
  case object Start
  case object Stop
}

class GameplayContext(
    val table: Table,
    val variation: Variation,
    val stake: Stake,
    val events: GameplayEvents = new GameplayEvents,
    val dealer: Dealer = new Dealer
    ) extends GameplayLike
                         with GameRotation
                         with Antes
                         with Blinds
                         with Dealing.DealContext
                         with BringIn
                         with Showdown {

  val round = new BettingRound(this)

  def prepareSeats(ctx: StageContext) {
    (table.seats: List[Seat]).filter (_ isReady) map (_ play)
  }

}
