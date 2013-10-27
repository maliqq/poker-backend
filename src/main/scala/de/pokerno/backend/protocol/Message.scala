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
  object AddBet extends ActionEvent {
    def apply(pos: Int, player: Player, bet: Bet) = {
      new proto.ActionEvent(proto.ActionEvent.ActionEventType.AddBet)
    }
  }
  
  object DiscardCards extends ActionEvent {
    def apply(pos: Int, player: Player, cards: List[Card]) = {
    }
  }
  
  /**
   * Table event
   * */
  object ButtonChange {
    def apply(i: Int) = {
      new proto.TableEvent(proto.TableEvent.TableEventType.ButtonChange)
    }
    
  }
  /**
   * Gameplay event
   * */
  object GameChange {
    def apply(game: Game) = new proto.GameplayEvent(proto.GameplayEvent.GameplayEventType.GameChange)
  }
  
  object StakeChange {
    def apply(stake: Game) = new proto.GameplayEvent(proto.GameplayEvent.GameplayEventType.StakeChange)
  }
  
  /**
   * Stage event
   * */
  object PlayStart {
    def apply(game: Game, stake: Stake) = new proto.StageEvent(
        proto.StageEvent.StageEventType.Start,
        proto.StageEvent.StageType.Play)
  }

  object PlayStop {
    def apply() = new proto.StageEvent(
        proto.StageEvent.StageEventType.Stop,
        proto.StageEvent.StageType.Play)
  }
  
  object StreetStart {
    def apply(name: String) = new proto.StageEvent(
        proto.StageEvent.StageEventType.Start,
        proto.StageEvent.StageType.Street)
  }

  /**
   * Deal event
   * */
  object DealCards {
    def apply(_type: Dealer.DealType, cards: List[Card] = List.empty, pos: Option[Int] = None, player: Option[Player] = None, cardsNum: Option[Int] = None) = {
      new proto.DealEvent
    }
  }

  object RequireBet {
    def apply(pos: Int, player: Player, call: Decimal, raise: Range) = {
      new proto.DealEvent
    }
  }
  
  object RequireDiscard {
    def apply(pos: Int, player: Player) = {
      new proto.DealEvent
    }
  }
  
  object DeclarePot {
    def apply(pot: Decimal, rake: Option[Decimal] = None) = {
      new proto.DealEvent
    }
  }
  
  object DeclareHand {
    def apply(pos: Int, player: Player, cards: List[Card], hand: Hand) = {
      new proto.DealEvent
    }
  }
  
  object DeclareWinner {
    def apply(pos: Int, player: Player, amount: Decimal) = {
      new proto.DealEvent
    }
  }
  /**
   * Command
   * */
  object JoinTable {
    def apply(pos: Int, amount: Decimal, player: Player) = {
      new de.pokerno.proto.JoinTable(pos, amount.toDouble)
    }
  }
}