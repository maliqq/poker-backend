package pokerno.backend.protocol

import scala.math.{BigDecimal => Decimal}
import pokerno.backend.poker.{Card, Hand}
import pokerno.backend.model._

object Message {
  abstract class Value extends Serializable
  
  trait Position {
    def pos: Option[Int]
  }
  
  trait Cards {
    def cards: List[Card]
  }
  
  case class DealCards (
    val _type: Dealer.DealType,
    val pos: Option[Int],
    val cards: List[Card]
  ) extends Value with Cards with Position

  case class RequireBet(
    val pos: Int,
    val call: Decimal,
    val min: Decimal,
    val max: Decimal
  ) extends Value
  
  case class AddBet(
      val _type: Bet.Value,
      val pos: Option[Int],
      val bet: Bet
  ) extends Value with Position
  
  case class MoveButton(
    val pos: Int
  ) extends Value
  
  case class ShowHand(
      val pos: Int,
      val hand: Hand,
      val cards: List[Card]
  ) extends Value
  
  case class CollectPot(val total: Decimal) extends Value
  
  case class Winner(
      val winner: Player,
      val amount: Decimal,
      val pos: Int
  ) extends Value
  
  case class PlayStart(
      val game: Game,
      val stake: Stake) extends Value
  case class StreetStart(
      val name: String) extends Value
  case class ChangeGame(
      val game: Game) extends Value
  
  // table state
  case class SitOut() extends Value
  case class ComeBack() extends Value
  case class JoinTable(
      val pos: Int,
      val player: Player,
      val amount: Decimal) extends Value
  case class LeaveTable() extends Value
  case class KickPlayer() extends Value
  case class SeatStateChange(pos: Int, state: Seat.State) extends Value

  case class ChatMessage() extends Value
  case class ErrorMessage() extends Value
  case class DealerMessage() extends Value
}
