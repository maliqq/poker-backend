package de.pokerno.backend.protocol

import scala.math.{ BigDecimal ⇒ Decimal }
import de.pokerno.poker.{ Card, Hand }
import de.pokerno.model._
import de.pokerno.proto

trait Message

class ActionEvent extends Message
class GameplayEvent extends Message
class StageEvent extends Message
class DealEvent extends Message
class Command extends Message

object Message {
  /**
   * Action event
   * */
  case class AddBet(pos: Int, player: Player, bet: Bet) {
    val schema = new proto.ActionEvent(proto.ActionEvent.ActionEventType.AddBet)
  }
  
  case class DiscardCards(pos: Int, player: Player, cards: List[Card]) {
  }
  
  /**
   * Table event
   * */
  case class ButtonChange(i: Int) {
    val schema = new proto.TableEvent(proto.TableEvent.TableEventType.ButtonChange)
  }
  /**
   * Gameplay event
   * */
  case class GameChange(game: Game) {
    val schema = new proto.GameplayEvent(proto.GameplayEvent.GameplayEventType.GameChange)
  }
  
  case class StakeChange(stake: Game) {
    val schema = new proto.GameplayEvent(proto.GameplayEvent.GameplayEventType.StakeChange)
  }
  
  /**
   * Stage event
   * */
  case class PlayStart(game: Game, stake: Stake) {
    val schema = new proto.StageEvent(
        proto.StageEvent.StageEventType.Start,
        proto.StageEvent.StageType.Play)
  }

  case class PlayStop() {
    val schema = new proto.StageEvent(
        proto.StageEvent.StageEventType.Stop,
        proto.StageEvent.StageType.Play)
  }
  
  case class StreetStart(name: String) {
    val schema = new proto.StageEvent(
        proto.StageEvent.StageEventType.Start,
        proto.StageEvent.StageType.Street)
  }

  /**
   * Deal event
   * */
  case class DealCards(_type: Dealer.DealType, cards: List[Card] = List.empty, pos: Option[Int] = None, player: Option[Player] = None, cardsNum: Option[Int] = None) {
    val schema = new proto.DealEvent
  }

  case class RequireBet(pos: Int, player: Player, call: Decimal, raise: Range) {
    val schema = new proto.DealEvent
  }
  
  case class RequireDiscard(pos: Int, player: Player) {
    val schema = new proto.DealEvent
  }
  
  case class DeclarePot(pot: Decimal, rake: Option[Decimal] = None) {
    val schema = new proto.DealEvent
  }
  
  case class DeclareHand(pos: Int, player: Player, cards: List[Card], hand: Hand) {
    val schema = new proto.DealEvent
  }
  
  case class DeclareWinner(pos: Int, player: Player, amount: Decimal) {
    val schema = new proto.DealEvent
  }
  /**
   * Command
   * */
  case class JoinTable(pos: Int, amount: Decimal, player: Player) {
    val schema = new de.pokerno.proto.JoinTable(pos, amount.toDouble)
  }
}
