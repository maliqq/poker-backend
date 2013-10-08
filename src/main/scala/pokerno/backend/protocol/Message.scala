package pokerno.backend.protocol

import scala.math.{ BigDecimal ⇒ Decimal }
import pokerno.backend.poker.{ Card, Hand }
import pokerno.backend.model._

object Message {
  abstract class Value extends Serializable

  // dealing and discarding
  case class DealCards(
    _type: Dealer.DealType,
    pos: Option[Int],
    cards: List[Card]) extends Value
  case class RequireDiscard(pos: Int) extends Value
  case class Discarded(pos: Int, num: Int) extends Value
  case class DiscardCards(pos: Int, cards: List[Card]) extends Value
  
  // bets
  case class RequireBet(
    pos: Int,
    call: Decimal,
    min: Decimal,
    max: Decimal) extends Value
  case class AddBet(
    pos: Int,
    bet: Bet) extends Value
  case class CollectPot(total: Decimal) extends Value

  // showdown
  case class ShowHand(
    pos: Int,
    hand: Hand,
    cards: List[Card]) extends Value
  case class ShowCards(pos: Int, cards: List[Card], muck: Boolean = false)
  case class Winner(
    winner: Player,
    amount: Decimal,
    pos: Int) extends Value
    
  // gameplay process
  case class PlayStart(
    game: Game,
    stake: Stake) extends Value
  case class PlayStop extends Value
  case class StreetStart(
    name: String) extends Value
  case class ChangeGame(
    game: Game) extends Value

  // table state
  case class MoveButton(
    pos: Int) extends Value
  case class SitOut() extends Value
  case class ComeBack() extends Value
  case class JoinTable(
    pos: Int,
    player: Player,
    amount: Decimal) extends Value
  case class LeaveTable() extends Value
  case class KickPlayer() extends Value
  case class SeatStateChange(pos: Int, state: Seat.State) extends Value

  // text messages
  case class ChatMessage() extends Value
  case class ErrorMessage() extends Value
  case class DealerMessage() extends Value
}
