package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import scala.concurrent.Future

object Gameplay {
  case object Start
  case object Stop
}

class Gameplay(
    val dealer: Dealer,
    val broadcast: Broadcast,
    val variation: Variation,
    val stake: Stake,
    val table: Table) extends GameRotation with Antes with Blinds with Dealing with BringIn with Betting with Showdown {

  var game: Game = variation match {
    case g: Game ⇒ g
    case m: Mix  ⇒ m.games.head
  }
  
  var betting: BettingRound = new BettingRound(table.seatAtButton)

  protected def moveButton {
    table.button.move
    broadcast all (Message.MoveButton(pos = table.button))
  }
  
  protected def setButton(pos: Int) {
    table.button.current = pos
    broadcast all (Message.MoveButton(pos = table.button))
  }
  
  protected def requireBet {
    val range = betting.range(game.limit, stake)
    val (call, min, max) = betting require (range)

    broadcast all(Message.RequireBet(call = call, min = min, max = max, pos = betting.pos))
  }

  protected def forceBet(betType: Bet.Value) {
    val bet = Bet force (betType, stake)
    betting force (bet)
    
    broadcast all (Message.AddBet(Bet.Ante, pos = Some(betting.pos), bet = bet))
  }
  
  protected def completeBetting {
    betting clear

    table.seats where (_ inPlay) map (_._1 play)

    val total = betting.pot total
    val message = Message.CollectPot(total = total)
    broadcast all (message)
  }
  
  def prepareSeats {
    table.seats where (_ isReady) map (_._1 play)
  }

}
