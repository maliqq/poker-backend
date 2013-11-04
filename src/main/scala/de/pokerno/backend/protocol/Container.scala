package de.pokerno.backend.protocol

import de.pokerno.model
import de.pokerno.poker

import scala.reflect._
import scala.math.{BigDecimal => Decimal}
import com.dyuproject.protostuff.ByteString;
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
class Hand {
  @BeanProperty var rank: HandSchema.RankType = null
  @BeanProperty var value: ByteString = null
  @BeanProperty var high: ByteString = null
  @BeanProperty var kicker: ByteString = null
  @BeanProperty var string: String = ""
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
  @BeanProperty var `type`: ActionEventSchema.EventType = null
  @BeanProperty var pos: Integer = null
  @BeanProperty var cardsNum: Integer = null
  @BeanProperty var amount: java.lang.Double = null
  @BeanProperty var bet: Bet = null
  @BeanProperty var cards: ByteString = null
  @BeanProperty var player: String = null
}

@MsgPack
class GameplayEvent extends Message {
  @BeanProperty var `type`: GameplayEventSchema.EventType = null
  @BeanProperty var game: Game = null
  @BeanProperty var stake: Stake = null
}

@MsgPack
class StageEvent extends Message {
  @BeanProperty var `type`: StageEventSchema.EventType = null
  @BeanProperty var stage: StageEventSchema.StageType = null
}

@MsgPack
class TableEvent extends Message {
  @BeanProperty var `type`: TableEventSchema.EventType = null
  @BeanProperty var button: Integer = null
  @BeanProperty var state: TableEventSchema.TableState = null
}

@MsgPack
class SeatEvent(_type: SeatEventSchema.EventType) extends Message {
  @BeanProperty var `type` = _type
  @BeanProperty var pos: Integer = null
  @BeanProperty var seat: Seat = null
  def this() = this(null)
}

@MsgPack
class DealEvent extends Message {
  @BeanProperty var `type`: DealEventSchema.EventType = null
  @BeanProperty var requireBet: RequireBet = null
  @BeanProperty var requireDiscard: RequireDiscard = null
  @BeanProperty var declarePot: DeclarePot = null
  @BeanProperty var declareWinner: DeclareWinner = null
}

@MsgPack
class Msg extends Message {
  @BeanProperty var `type`: MsgSchema.MsgType = null
  @BeanProperty var body: String = null
}

@MsgPack
class Cmd extends Message {
  @BeanProperty var `type`: CmdSchema.CmdType = null
  @BeanProperty var joinTable: JoinTable = null
  @BeanProperty var actionEvent: ActionEvent = null
}

@MsgPack
class Event extends Message {
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
  def player_=(p: model.Player)
  def getPlayer: String = player.toString
  def setPlayer(v: String) = player = new model.Player(v)
}

trait HasAmount {
  def amount: Decimal
  def amount_=(v: Decimal)
  def getAmount: Double = amount.toDouble
  def setAmount(v: Double) = amount = v
}

trait HasCards {
  def cards: List[poker.Card]
  def cards_=(v: List[poker.Card])
  def getCards: ByteString = cards.map(_.toByte).asInstanceOf[ByteString]
  def setCards(v: ByteString) = {}// FIXME: cards = v.map(poker.Card.wrap(_))
}
