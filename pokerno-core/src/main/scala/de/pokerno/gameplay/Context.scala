package de.pokerno.gameplay

import de.pokerno.model._

object Gameplay {
  case object Start
  case object Stop
}

trait ContextLike extends Button {
  //
  val events: Events
  // game or mix
  val variation: Variation
  // current game
  var game: Game = variation match {
    case g: Game ⇒ g
    case m: Mix  ⇒ m.games.head
  }
  // stake
  val stake: Stake
  //
  val table: Table
  //
  val dealer: Dealer
  //
  val round: BettingRound
}

class Context(
    val table: Table, 
    val variation: Variation, 
    val stake: Stake, 
    val events: Events = new Events, 
    val dealer: Dealer = new Dealer
                    ) extends ContextLike
                         with GameRotation
                         with Betting
                         //with Antes
                         //with Blinds
                         with Dealing.DealContext
                         with BringIn
                         with Showdown {

  val round = new BettingRound(table, game, stake)

  def prepareSeats(ctx: StageContext) {
    (table.seats: List[Seat]).filter (_ isReady) map (_ play)
  }
  
  override def toString = {
    val b = new StringBuilder
    b.append("table:\n%s\n" format table)
    b.append("stake: %s\n" format stake)
    b.append("game: %s\n" format game)
    b.toString
  }

}
