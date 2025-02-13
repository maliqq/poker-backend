package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.hub.Exchange
import de.pokerno.payment.thrift.Payment.{FutureIface => Balance}

trait ContextLike extends context.Button {
  //
  val events: Publisher[_]
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
  val play: Play
  val mode: Mode.Value
  val bettingRound: betting.Round
  val discardingRound: discarding.Round

  val balance: Balance

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
    val balance: Balance,
    val events: Publisher[_],
    val play: Play = new Play,
    val mode: Mode.Value = Mode.Cash
  ) extends ContextLike {

  lazy val bettingRound: betting.Round = new betting.Round(table, game, stake, play)
  lazy val discardingRound: discarding.Round = new discarding.Round(table, game, play.dealer)
  lazy val gameRotation = new Rotation(variation.asInstanceOf[Mix].games)

}
