package de.pokerno.backend.protocol

import de.pokerno.model
import de.pokerno.poker

import scala.reflect._
import scala.math.{BigDecimal => Decimal}
import com.dyuproject.protostuff
import org.msgpack.annotation.{ Message => MsgPack }

@MsgPack
case class Bet(
  @BeanProperty var `type`: BetSchema.BetType,
  @BeanProperty var amount: java.lang.Double
) {
  def this() = this(null, .0)
}

@MsgPack
class Game {
  @BeanProperty var `type`: GameSchema.GameType = null
  @BeanProperty var limit: GameSchema.GameLimit = null
  @BeanProperty var tableSize: Int = 0
}

@MsgPack
case class Hand(
  @BeanProperty var cards: protostuff.ByteString,
  @BeanProperty var rank: HandSchema.RankType,
  @BeanProperty var value: protostuff.ByteString,
  @BeanProperty var high: protostuff.ByteString,
  @BeanProperty var kicker: protostuff.ByteString,
  @BeanProperty var string: String = ""
) {
  def this() = this(null, null, null, null, null, "")
}

@MsgPack
case class Range(
  @BeanProperty var min: Double,
  @BeanProperty var max: Double
) {
  def this() = this(.0, .0)
}

@MsgPack
class Seat {
  @BeanProperty var state: SeatSchema.SeatState = null
  @BeanProperty var player: String = null
  @BeanProperty var stackAmount: java.lang.Double = null
  @BeanProperty var putAmount: java.lang.Double = null
}

@MsgPack
class Stake {
  @BeanProperty var bigBlind: java.lang.Double = null
  @BeanProperty var smallBlind: java.lang.Double = null
  @BeanProperty var ante: java.lang.Double = null
  @BeanProperty var bringIn: java.lang.Double = null
}

@MsgPack
class Table {
  @BeanProperty var size: Int = 0
  @BeanProperty var button: Int = 0
  @BeanProperty var seats: java.util.ArrayList[Seat] = null
}

@MsgPack
class ActionEvent extends Message {
  def schema = ActionEventSchema.SCHEMA
  //def pipeSchema = ActionEventSchema.PIPE_SCHEMA
  @BeanProperty var `type`: ActionEventSchema.EventType = null
  @BeanProperty var discardCards: DiscardCards = null
  @BeanProperty var showCards: ShowCards = null
  @BeanProperty var addBet: AddBet = null
}

@MsgPack
class GameplayEvent extends Message {
  def schema = GameplayEventSchema.SCHEMA
  //def pipeSchema = GameplayEventSchema.PIPE_SCHEMA
  @BeanProperty var `type`: GameplayEventSchema.EventType = null
  @BeanProperty var game: Game = null
  @BeanProperty var stake: Stake = null
}

@MsgPack
class StageEvent extends Message {
  def schema = StageEventSchema.SCHEMA
  //def pipeSchema = StageEventSchema.PIPE_SCHEMA
  @BeanProperty var `type`: StageEventSchema.EventType = null
  @BeanProperty var stage: StageEventSchema.StageType = null
  @BeanProperty var street: StageEventSchema.StreetType = null
}

@MsgPack
class TableEvent extends Message {
  def schema = TableEventSchema.SCHEMA
  //def pipeSchema = TableEventSchema.PIPE_SCHEMA
  @BeanProperty var `type`: TableEventSchema.EventType = null
  @BeanProperty var button: Integer = null
  @BeanProperty var state: TableEventSchema.TableState = null
}

@MsgPack
class SeatEvent(_type: SeatEventSchema.EventType) extends Message {
  def schema = SeatEventSchema.SCHEMA
  //def pipeSchema = SeatEventSchema.PIPE_SCHEMA
  @BeanProperty var `type` = _type
  @BeanProperty var pos: Integer = null
  @BeanProperty var seat: Seat = null
  def this() = this(null)
}

@MsgPack
class DealEvent extends Message {
  def schema = DealEventSchema.SCHEMA
  //def pipeSchema = DealEventSchema.PIPE_SCHEMA
  @BeanProperty var `type`: DealEventSchema.EventType = null
  @BeanProperty var requireBet: RequireBet = null
  @BeanProperty var requireDiscard: RequireDiscard = null
  @BeanProperty var declarePot: DeclarePot = null
  @BeanProperty var declareWinner: DeclareWinner = null
}

@MsgPack
class Msg extends Message {
  def schema = MsgSchema.SCHEMA
  //def pipeSchema = MsgSchema.PIPE_SCHEMA
  @BeanProperty var `type`: MsgSchema.MsgType = null
  @BeanProperty var body: String = null
}

@MsgPack
class Cmd extends Message {
  def schema = CmdSchema.SCHEMA
  //def pipeSchema = CmdSchema.PIPE_SCHEMA
  @BeanProperty var `type`: CmdSchema.CmdType = null
  @BeanProperty var joinTable: JoinTable = null
  @BeanProperty var actionEvent: ActionEvent = null
}

@MsgPack
class Event extends Message {
  def schema = EventSchema.SCHEMA
  //def pipeSchema = EventSchema.PIPE_SCHEMA
  @BeanProperty var `type`: EventSchema.EventType = null
  @BeanProperty var seatEvent: SeatEvent = null
  @BeanProperty var actionEvent: ActionEvent = null
  @BeanProperty var stageEvent: StageEvent = null
  @BeanProperty var tableEvent: TableEvent = null
  @BeanProperty var dealEvent: DealEvent = null
  @BeanProperty var gameplayEvent: GameplayEvent = null
}

trait HasPlayer {
  def player: model.Player
  def player_=(v: model.Player)
  
  def getPlayer: String = if (player != null) player.id else null
  def setPlayer(v: String) = player = new model.Player(v)
}

trait HasAmount {
  def amount: Decimal
  def amount_=(v: Decimal)
  
  def getAmount: java.lang.Double = if (amount != null) amount.toDouble else null
  def setAmount(v: java.lang.Double) = amount = v.toDouble
}

trait HasCards {
  import Implicits._
  
  def cards: List[poker.Card]
  def cards_=(v: List[poker.Card])
  
  def getCards: protostuff.ByteString = cards
  def setCards(v: protostuff.ByteString) = cards = v
}
