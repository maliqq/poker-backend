package de.pokerno.backend.protocol

import de.pokerno.model
import de.pokerno.poker
import scala.reflect._
import scala.math.{BigDecimal => Decimal}
import com.dyuproject.protostuff.ByteString;

case class Bet(
  @BeanProperty var bType: BetType,
  @BeanProperty var amount: java.lang.Double
) {
  def this() = this(null, .0)
}

class Game {
  @BeanProperty var gType: GameType = null
  @BeanProperty var limit: GameLimit = null
  @BeanProperty var tableSize: Int = 0
}

class Hand {
  @BeanProperty var rank: RankType = null
  @BeanProperty var value: List[Byte] = null
  @BeanProperty var high: List[Byte] = null
  @BeanProperty var kicker: List[Byte] = null
  @BeanProperty var string: String = ""
}

case class Range(
  @BeanProperty var min: Double,
  @BeanProperty var max: Double
) {
  def this() = this(.0, .0)
}

class Seat {
  @BeanProperty var state: SeatState = null
  @BeanProperty var player: String = null
  @BeanProperty var stackAmount: java.lang.Double = null
  @BeanProperty var putAmount: java.lang.Double = null
}

class Stake {
  @BeanProperty var bigBlind: java.lang.Double = null
  @BeanProperty var smallBlind: java.lang.Double = null
  @BeanProperty var ante: java.lang.Double = null
  @BeanProperty var bringIn: java.lang.Double = null
}

class Table {
  @BeanProperty var size: Int = 0
  @BeanProperty var button: Int = 0
  @BeanProperty var seats: List[Seat] = null
}

trait ActionEventBase extends Message {
  @BeanProperty var eType: ActionEventType
  
  @BeanProperty var pos: Integer
  
  @BeanProperty var cardsNum: Integer = null
  
  def amount: Any
  def getAmount: java.lang.Double = null
  def setAmount(v: java.lang.Double) = {}
  
  def bet: Any
  def getBet: Bet = null
  def setBet(b: Bet) = {}
  
  lazy val schema = new ActionEventSchema
}

class ActionEvent extends ActionEventBase with HasPlayer with HasCards {
  @BeanProperty override var eType: ActionEventType = null
  @BeanProperty override var pos: Integer = null
  @BeanProperty override var amount: java.lang.Double = null
  @BeanProperty override var bet: Bet = null
  @BeanProperty var cards: ByteString = null
  @BeanProperty var cardsNum: Integer = null
  @BeanProperty var player: String = null
}

trait GameplayEventBase extends Message {
  lazy val schema = new GameplayEventSchema
  def eType: GameplayEventType
}

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

class StageEvent extends StageEventBase {
  @BeanProperty var eType: StageEventType = null
  @BeanProperty var stage: StageType = null
}

trait TableEventBase extends Message {
  lazy val schema = new TableEventSchema
  def eType: TableEventType
}

class TableEvent extends TableEventBase {
  @BeanProperty var eType: TableEventType = null
}

trait DealEventBase extends Message {
  lazy val schema = new DealEventSchema
  def eType: DealEventType
}

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

class Msg extends MsgBase {
  @BeanProperty var mType: MsgType = null
  @BeanProperty var body: String = null
}

trait CmdBase extends Message {
  lazy val schema = new CmdSchema
  def cType: CmdType
}

class Cmd extends CmdBase {
  @BeanProperty var cType: CmdType = null
  @BeanProperty var joinTable: JoinTable = null
  @BeanProperty var actionEvent: ActionEvent = null
}

trait SeatEventBase extends Message {
  lazy val schema = new SeatEventSchema
  def eType: SeatEventType
  @BeanProperty var pos: Integer = null
  @BeanProperty var seat: Seat = null
}

class SeatEvent(_type: SeatEventType) extends SeatEventBase {
  @BeanProperty var eType = _type
  
  def this() = this(null)
}

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
