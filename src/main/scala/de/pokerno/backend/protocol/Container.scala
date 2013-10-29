package de.pokerno.backend.protocol

import de.pokerno.model
import de.pokerno.poker
import scala.reflect._
import scala.math.{BigDecimal => Decimal}

case class Bet(
  @BeanProperty var bType: BetType = null,
  @BeanProperty var amount: Double = .0
)

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

class Range {
  @BeanProperty var min: Double = .0
  @BeanProperty var max: Double = .0
}

class Seat {
  @BeanProperty var state: SeatState = null
  @BeanProperty var player: String = ""
  @BeanProperty var stackAmount: Double = .0
  @BeanProperty var putAmount: Double = .0
}

class Stake {
  @BeanProperty var bigBlind: Double = .0
  @BeanProperty var smallBlind: Double = .0
  @BeanProperty var ante: Double = .0
  @BeanProperty var bringIn: Double = .0
}

class Table {
  @BeanProperty var size: Int = 0
  @BeanProperty var button: Int = 0
  @BeanProperty var seats: List[Seat] = null
}

trait ActionEventBase extends Message {
  @BeanProperty var eType: ActionEventType
  
  @BeanProperty var pos: Int
  
  @BeanProperty var cardsNum: Int = 0
  
  def getAmount: Double = .0
  def setAmount(v: Double) = {}
  
  def getBet: Bet = null
  def setBet(b: Bet) = {}
  
  lazy val schema = new ActionEventSchema
}

abstract class ActionEvent extends ActionEventBase with HasPlayer with HasCards {
}

abstract class GameplayEvent extends Message {
  lazy val schema = new GameplayEventSchema
  @BeanProperty var eType: GameplayEventType
}

abstract class StageEvent extends Message {
  lazy val schema = new StageEventSchema
  @BeanProperty var eType: StageEventType
}

abstract class TableEvent extends Message {
  lazy val schema = new TableEventSchema
  @BeanProperty var eType: TableEventType = null
}

abstract class DealEvent extends Message {
  lazy val schema = new DealEventSchema
  @BeanProperty var eType: DealEventType
}

abstract class Msg extends Message {
  lazy val schema = new MsgSchema
  @BeanProperty var mType: MsgType
}

abstract class Cmd extends Message {
  lazy val schema = new CmdSchema
  @BeanProperty var cType: CmdType
}

abstract class SeatEventBase extends Message {
  lazy val schema = new SeatEventSchema
  @BeanProperty var eType: SeatEventType
  @BeanProperty var pos: Int = 0
  @BeanProperty var seat: Seat = null
}

class SeatEvent(_type: SeatEventType) extends SeatEventBase {
  eType = _type
  
  def this() = this(null)
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
  def getCards: List[Byte] = cards.map(_.toByte)
  def setCards(v: List[Byte]) = cards = v.map(poker.Card.wrap(_))
}
