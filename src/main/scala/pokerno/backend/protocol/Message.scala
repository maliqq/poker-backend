package pokerno.backend.protocol

import scala.math.{ BigDecimal ⇒ Decimal }
import pokerno.backend.poker.{ Card, Hand }
import pokerno.backend.model._

object Message {
  abstract class Value extends Serializable

  case class DealCards(
    _type: Dealer.DealType,
    pos: Option[Int],
    cards: List[Card]) extends Value

  case class RequireDiscard(pos: Int) extends Value
  case class DiscardCards(pos: Int, cards: List[Card]) extends Value
    
  case class RequireBet(
    pos: Int,
    call: Decimal,
    min: Decimal,
    max: Decimal) extends Value

  case class AddBet(
    pos: Int,
    bet: Bet) extends Value

  case class MoveButton(
    pos: Int) extends Value

  case class ShowHand(
    pos: Int,
    hand: Hand,
    cards: List[Card]) extends Value

  case class CollectPot(total: Decimal) extends Value

  case class Winner(
    winner: Player,
    amount: Decimal,
    pos: Int) extends Value

  case class PlayStart(
    game: Game,
    stake: Stake) extends Value
  case class StreetStart(
    name: String) extends Value
  case class ChangeGame(
    game: Game) extends Value

  // table state
  case class SitOut() extends Value
  case class ComeBack() extends Value
  case class JoinTable(
    pos: Int,
    player: Player,
    amount: Decimal) extends Value
  case class LeaveTable() extends Value
  case class KickPlayer() extends Value
  case class SeatStateChange(pos: Int, state: Seat.State) extends Value

  case class ChatMessage() extends Value
  case class ErrorMessage() extends Value
  case class DealerMessage() extends Value
}
