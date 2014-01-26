package de.pokerno.gameplay

import de.pokerno.model._

object Gameplay {
  case object Start
  case object Stop
}

class GameplayContext(
    val events: GameplayEvents,
    val variation: Variation,
    val stake: Stake,
    val table: Table) extends GameplayLike
                         with GameRotation
                         with Antes
                         with Blinds
                         with Dealing
                         with BringIn
                         with Showdown {

  lazy val dealer: Dealer = new Dealer
  val round = new BettingRound(this)

  def prepareSeats {
    (table.seats: List[Seat]).filter (_ isReady) map (_ play)
  }

}
