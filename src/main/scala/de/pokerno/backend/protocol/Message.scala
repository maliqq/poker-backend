package de.pokerno.backend.protocol

import scala.math.{ BigDecimal ⇒ Decimal }
import de.pokerno.backend.poker.{ Card, Hand }
import de.pokerno.backend.model._

import org.msgpack.annotation.{ Message => MsgPack }

abstract class Message extends Serializable {
}

object Message {
  // dealing and discarding
  case class DealCards(
      _type: Dealer.DealType,
      cards: List[Card],
      pos: Option[Int]) extends Message


  case class RequireDiscard(pos: Int) extends Message


  case class Discarded(pos: Int, num: Int) extends Message


  case class DiscardCards(pos: Int, cards: List[Card]) extends Message

  // bets

  case class RequireBet(
      pos: Int,
      call: Decimal,
      raise: Range) extends Message


  case class Acting(pos: Int) extends Message


  case class AddBet(
      pos: Int,
      bet: Bet) extends Message


  case class CollectPot(total: Decimal) extends Message

  // showdown

  case class ShowHand(
      pos: Int,
      cards: List[Card],
      hand: Hand) extends Message


  case class ShowCards(pos: Int, cards: List[Card], muck: Boolean = false) extends Message


  case class Winner(
      pos: Int,
      winner: Player,
      amount: Decimal) extends Message

  // gameplay process

  case class PlayStart(
      game: Game,
      stake: Stake) extends Message


  case class PlayStop() extends Message


  case class StreetStart(
      name: String) extends Message


  case class ChangeGame(
      game: Game) extends Message

  
  case class MoveButton(
      pos: Int) extends Message


  case class SitOut() extends Message


  case class ComeBack() extends Message


  case class JoinTable(
      pos: Int,
      player: Player,
      amount: Decimal) extends Message


  case class LeaveTable() extends Message


  case class KickPlayer() extends Message


  case class SeatStateChange(pos: Int, state: Seat.State) extends Message

  // text messages

  case class ChatMessage() extends Message


  case class ErrorMessage() extends Message


  case class DealerMessage() extends Message
}
