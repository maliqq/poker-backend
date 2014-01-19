package de.pokerno.gameplay

import de.pokerno.model._

trait GameplayLike extends Button {

  //
  val events: GameplayEvents
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
