package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import scala.concurrent.Future
import scala.math.{ BigDecimal ⇒ Decimal }

object Gameplay {
  case object Start
  case object Stop
}

trait GameplayLike {
  val events: EventBus
  val variation: Variation
  var game: Game
  val stake: Stake
  val table: Table
  val dealer: Dealer
  val round: BettingRound
}

class Gameplay(
    val events: EventBus,
    val variation: Variation,
    val stake: Stake,
    val table: Table) extends GameplayLike with GameRotation with Antes with Blinds with Dealing with BringIn with Showdown {

  val dealer: Dealer = new Dealer

  var game: Game = variation match {
    case g: Game ⇒ g
    case m: Mix  ⇒ m.games.head
  }

  val round = new BettingRound(this)

  def prepareSeats {
    table.seats where (_ isReady) map (_._1 play)
  }

}
