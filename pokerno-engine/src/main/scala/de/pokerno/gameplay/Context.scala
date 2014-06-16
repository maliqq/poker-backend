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
  val play: Play
  val mode: Mode.Value
  val bettingRound: betting.Round
  val discardingRound: discarding.Round
  
  def isTournament = mode == Mode.Tournament
  def isCash = mode == Mode.Cash
  
  def gameOptions = game.options

  override def toString = f"table:${table} game: ${game} stake: ${stake}"
}

// TODO
//private[gameplay]
class Context(
    val id: String,
    val table: Table,
    val variation: Variation,
    val stake: Stake,
    val events: Events,
    val dealer: Dealer = new Dealer,
    val play: Play = new Play,
    val mode: Mode.Value = Mode.Cash
  ) extends ContextLike {
  
  def this(id: String, table: Table, variation: Variation, stake: Stake) = this(
      id,
      table,
      variation,
      stake,
      new Events(id)
      )
  
  lazy val bettingRound: betting.Round = new betting.Round(table, game, stake)
  lazy val discardingRound: discarding.Round = new discarding.Round(table, game, dealer)
  lazy val gameRotation = new Rotation(variation.asInstanceOf[Mix].games)

}
