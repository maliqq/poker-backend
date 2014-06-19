package de.pokerno.gameplay.tournament

import akka.actor.{ Actor, ActorRef, ActorLogging }

import de.pokerno.model._
import concurrent.duration._

object Tournament {
  case class Entry(var stack: Decimal) {
    var rebuysCount: Int = 0
    var addon: Boolean = false
    var knockoutsCount: Int = 0
  }

  case class BuyIn(
      price: Decimal, // buy in price
      stack: Int, // stack for entry and re-entry (rebuy)
      fee: Option[Decimal] = None, // buy in fee
      addonStack: Option[Int] = None, // stack for add-on
      bounty: Option[Decimal] = None // price for knock out
      ) {
  }

  object Format extends Enumeration {
    val Freezout, Knockout, Shootout = Value
  }

  case class RebuysPolicy(
    rebuys: Boolean = false,
    addons: Boolean = false,
    rebuyPeriod: Duration,
    addonBreakPeriod: Duration,
    maxRebuys: Int)

  class Level(
      val smallBlind: Int,
      val bigBlind: Int,
      val ante: Option[Int] = None) {
  }

  class Structure(
    val level: Duration,
    val break: Duration)

  import Format._
}

class Tournament(val game: Game, val buyIn: Tournament.BuyIn, val format: Tournament.Format.Value) extends Actor with ActorLogging {
  val entries: collection.mutable.Map[Player, Tournament.Entry] = collection.mutable.Map.empty
  val tables: List[Table] = List.empty

  override def preStart() {

  }

  case class Register(player: Player)
  case class Rebuy(player: Player)
  case class Addon(player: Player)
  case class Knockout(winner: Player, looser: Player)
  case class Eliminate(player: Player)

  def bucketize(total: Int, perBucket: Int) = List.range(0, total).
    zipWithIndex.
    groupBy { x ⇒ Math.floor(x._2 / total.toDouble * perBucket) }.
    map { case (k, v) ⇒ (k, v.map(_._1)) }

  def initialSeating() {
    val buckets = bucketize(entries.size, game.tableSize)

  }

  def receive = {
    case Register(player) ⇒
      entries += (player -> Tournament.Entry(buyIn.stack))

    case Rebuy(player) ⇒
      val entry = entries(player)
      entry.rebuysCount += 1
      entry.stack += buyIn.stack

    case Addon(player) ⇒
      val entry = entries(player)
      if (!entry.addon) {
        entry.addon = true
        entry.stack += buyIn.addonStack.get
      }

    case Knockout(winner, looser) ⇒
      val entry = entries(winner)
      entry.knockoutsCount += 1

    case Eliminate(player) ⇒
  }
}
