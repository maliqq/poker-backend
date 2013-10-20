package pokerno.backend.protocol

import scala.math.{ BigDecimal ⇒ Decimal }
import pokerno.backend.poker.{ Card, Hand }
import pokerno.backend.model._

object Message {
  abstract class Value extends Serializable {
    def key: String
  }

  // dealing and discarding
  case class DealCards(
    _type: Dealer.DealType,
    cards: List[Card],
    pos: Option[Int]) extends Value {
    final val key = "deal_cards"
  }
  
  case class RequireDiscard(pos: Int) extends Value {
    final val key = "require_discard"
  }
  
  case class Discarded(pos: Int, num: Int) extends Value {
    final val key = "discarded"
  }
  
  case class DiscardCards(pos: Int, cards: List[Card]) extends Value {
    final val key = "discard_cards"
  }

  // bets
  case class RequireBet(
    pos: Int,
    call: Decimal,
    raise: Range) extends Value {
    final val key = "require_bet"
  }
  
  case class Acting(pos: Int) extends Value {
    final val key = "acting"
  }
  
  case class AddBet(
    pos: Int,
    bet: Bet) extends Value {
    final val key = "add_bet"
  }
  
  case class CollectPot(total: Decimal) extends Value {
    final val key = "collect_pot"
  }

  // showdown
  case class ShowHand(
    pos: Int,
    cards: List[Card],
    hand: Hand) extends Value {
    final val key = "show_hand"
  }
  
  case class ShowCards(pos: Int, cards: List[Card], muck: Boolean = false) {
    final val key = "show_cards"
  }
  
  case class Winner(
    pos: Int,
    winner: Player,
    amount: Decimal) extends Value {
    final val key = "winner"
  }

  // gameplay process
  case class PlayStart(
    game: Game,
    stake: Stake) extends Value {
    final val key = "play_start"
  }
  
  case class PlayStop() extends Value {
    final val key = "play_stop"
  }
  
  case class StreetStart(
    name: String) extends Value {
    final val key = "steet_start"
  }
  
  case class ChangeGame(
    game: Game) extends Value {
    final val key = "change_game"
  }

  // table state
  case class MoveButton(
    pos: Int) extends Value {
    final val key = "move_button"
  }
  
  case class SitOut() extends Value {
    final val key = "sit_out"
  }
  
  case class ComeBack() extends Value {
    final val key = "come_back"
  }
  
  case class JoinTable(
    pos: Int,
    player: Player,
    amount: Decimal) extends Value {
    final val key = "join_table"
  }
  
  case class LeaveTable() extends Value {
    final val key = "leave_table"
  }
  
  case class KickPlayer() extends Value {
    final val key = "kick_player"
  }
  
  case class SeatStateChange(pos: Int, state: Seat.State) extends Value {
    final val key = "seat_state_change"
  }

  // text messages
  case class ChatMessage() extends Value {
    final val key = "chat_message"
  }
  
  case class ErrorMessage() extends Value {
    final val key = "error_message"
  }
  
  case class DealerMessage() extends Value {
    final val key = "dealer_message"
  }
}
