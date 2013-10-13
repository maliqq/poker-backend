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

  val betting: ActorRef
  val round = new BettingRound(table)

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

  def requireBet(call: Decimal, range: Range) {
    val player = round.acting._1.player.get

    broadcast.except(player) {
      Message.Acting(pos = round current)
    }

    broadcast.one(player) {
      Message.RequireBet(pos = round current, call = call, range = range)
    }
  }

  def forceBet(acting: Tuple2[Seat, Int], betType: Bet.Value) {
    val bet = Bet force (betType, stake)

    round.acting = acting
    betting ! bet
    broadcast all (Message.AddBet(pos = round current, bet = bet))
  }

  def bettingComplete(pot: Pot) {
    table.seats where (_ inPlay) map (_._1 play)

    val message = Message.CollectPot(total = pot total)
    broadcast all (message)
  }

  def prepareSeats {
    table.seats where (_ isReady) map (_._1 play)
  }

}
