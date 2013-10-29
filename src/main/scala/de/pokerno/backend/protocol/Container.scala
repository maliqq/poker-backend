package de.pokerno.backend.protocol

import de.pokerno.model
import de.pokerno.poker

import scala.reflect._
import scala.math.{BigDecimal => Decimal}
import com.dyuproject.protostuff.ByteString;
import org.msgpack.annotation.{ Message => MsgPack }

@MsgPack
case class Bet(
  @BeanProperty var bType: BetType,
  @BeanProperty var amount: java.lang.Double
) {
  def this() = this(null, .0)
}

@MsgPack
class Game {
  @BeanProperty var gType: GameType = null
  @BeanProperty var limit: GameLimit = null
  @BeanProperty var tableSize: Int = 0
}

@MsgPack
class Hand {
  @BeanProperty var rank: RankType = null
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
  @BeanProperty var state: SeatState = null
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

trait ActionEventBase extends Message {
  @BeanProperty var eType: ActionEventType
  
  @BeanProperty var pos: Integer
  
  @BeanProperty var cardsNum: Integer = null
  
  def getAmount: java.lang.Double = null
  def setAmount(v: java.lang.Double) = {}
  
  def getBet: Bet = null
  def setBet(b: Bet) = {}
  
  lazy val schema = new ActionEventSchema
}

@MsgPack
class ActionEvent extends ActionEventBase {
  @BeanProperty override var eType: ActionEventType = null
  @BeanProperty override var pos: Integer = null
  
  var amount: java.lang.Double = null
  override def getAmount: java.lang.Double = amount
  override def setAmount(v: java.lang.Double) = amount = v
  
  var bet: Bet = null
  override def getBet: Bet = bet
  override def setBet(v: Bet) = bet = v
  
  @BeanProperty var cards: ByteString = null
  @BeanProperty var player: String = null
}

trait GameplayEventBase extends Message {
  lazy val schema = new GameplayEventSchema
  def eType: GameplayEventType
}

@MsgPack
class GameplayEvent extends GameplayEventBase {
  @BeanProperty var eType: GameplayEventType = null
  @BeanProperty var game: Game = null
  @BeanProperty var stake: Stake = null
}

trait StageEventBase extends Message {
  lazy val schema = new StageEventSchema
  def eType: StageEventType
  def stage: StageType
}

@MsgPack
class StageEvent extends StageEventBase {
  @BeanProperty var eType: StageEventType = null
  @BeanProperty var stage: StageType = null
}

trait TableEventBase extends Message {
  lazy val schema = new TableEventSchema
  def eType: TableEventType
}

@MsgPack
class TableEvent extends TableEventBase {
  @BeanProperty var eType: TableEventType = null
  @BeanProperty var button: Integer = null
  @BeanProperty var state: TableState = null
}

trait SeatEventBase extends Message {
  lazy val schema = new SeatEventSchema
  def eType: SeatEventType
  @BeanProperty var pos: Integer = null
  @BeanProperty var seat: Seat = null
}

@MsgPack
class SeatEvent(_type: SeatEventType) extends SeatEventBase {
  @BeanProperty var eType = _type
  
  def this() = this(null)
}

trait DealEventBase extends Message {
  lazy val schema = new DealEventSchema
  def eType: DealEventType
}

@MsgPack
class DealEvent extends DealEventBase {
  @BeanProperty var eType: DealEventType = null
  @BeanProperty var requireBet: RequireBet = null
  @BeanProperty var requireDiscard: RequireDiscard = null
  @BeanProperty var declarePot: DeclarePot = null
  @BeanProperty var declareWinner: DeclareWinner = null
}

trait MsgBase extends Message {
  lazy val schema = new MsgSchema
  def mType: MsgType
}

@MsgPack
class Msg extends MsgBase {
  @BeanProperty var mType: MsgType = null
  @BeanProperty var body: String = null
}

trait CmdBase extends Message {
  lazy val schema = new CmdSchema
  def cType: CmdType
}

@MsgPack
class Cmd extends CmdBase {
  @BeanProperty var cType: CmdType = null
  @BeanProperty var joinTable: JoinTable = null
  @BeanProperty var actionEvent: ActionEvent = null
}

@MsgPack
class Event extends Message {
  @BeanProperty var eType: EventType = null
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
