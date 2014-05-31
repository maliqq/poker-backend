package de.pokerno.gameplay

import de.pokerno.model._

trait ContextLike extends context.Button {
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
  val round: betting.Round
}

// TODO
//private[gameplay]
class Context(
    val table: Table,
    val variation: Variation,
    val stake: Stake,
    val events: Events,
    val dealer: Dealer = new Dealer
  ) extends ContextLike {

  lazy val gameRotation = new Rotation(variation.asInstanceOf[Mix].games)
  lazy val round = new betting.Round(table, game, stake)

  override def toString = f"table:${table} stake: ${stake} game: ${game}"

}
