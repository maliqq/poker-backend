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

class Gameplay(
    val dealer: Dealer,
    val broadcast: Broadcast,
    val variation: Variation,
    val stake: Stake,
    val table: Table) extends GameRotation with Antes with Blinds with Dealing with BringIn with Showdown {

  var game: Game = variation match {
    case g: Game ⇒ g
    case m: Mix  ⇒ m.games.head
  }

  val round = new BettingRound(this)

  def moveButton {
    table.button.move
    round.current = table.button
    broadcast all (Message.MoveButton(pos = table.button))
  }

  def setButton(pos: Int) {
    table.button.current = pos
    round.current = pos
    broadcast all (Message.MoveButton(pos = table.button))
  }

  def prepareSeats {
    table.seats where (_ isReady) map (_._1 play)
  }

}
