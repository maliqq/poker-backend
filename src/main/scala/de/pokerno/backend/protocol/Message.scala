package de.pokerno.backend.protocol

import scala.math.{ BigDecimal ⇒ Decimal }
import de.pokerno.poker
import de.pokerno.model
import scala.reflect._
import org.msgpack.annotation.{ Message => MsgPack }

trait Message

/**
 * Action event
 * */
@MsgPack
case class AddBet(
    @BeanProperty var pos: Int,
    var player: model.Player,
    var bet: model.Bet) extends ActionEventBase with HasPlayer {
  def this() = this(0, null, null)
  override def getBet: Bet = new Bet()
  override def setBet(v: Bet) = {}
  @BeanProperty var eType = ActionEventType.ADD_BET
}

@MsgPack
case class DiscardCards(
    @BeanProperty var pos: Integer,
    var player: model.Player,
    var cards: List[poker.Card]) extends ActionEventBase with HasPlayer with HasCards {
  def this() = this(null, null, null)
  @BeanProperty var eType = ActionEventType.DISCARD_CARDS
}

/**
 * Table event
 * */
@MsgPack
case class ButtonChange(
    @BeanProperty button: Int) extends TableEventBase {
  def this() = this(0)
  @BeanProperty var eType = TableEventType.BUTTON
}
/**
 * Gameplay event
 * */
@MsgPack
case class GameChange(
    game: model.Game) extends GameplayEventBase {
  def this() = this(null)
  @BeanProperty var eType = GameplayEventType.GAME
}

@MsgPack
case class StakeChange(
    stake: model.Game) extends GameplayEventBase {
  def this() = this(null)
  @BeanProperty var eType = GameplayEventType.STAKE
}

/**
 * Stage event
 * */
@MsgPack
case class PlayStart(
    game: model.Game,
    stake: model.Stake) extends StageEventBase {
  def this() = this(null, null)
  @BeanProperty var stage = StageType.PLAY
  @BeanProperty var eType = StageEventType.START
}

@MsgPack
case class PlayStop() extends StageEventBase {
  @BeanProperty var stage = StageType.PLAY
  @BeanProperty var eType = StageEventType.STOP
  //def this() = this()
}

@MsgPack
case class StreetStart(name: String) extends StageEventBase {
  @BeanProperty var stage = StageType.STREET
  @BeanProperty var eType = StageEventType.START
  def this() = this("")
}

/**
 * Deal event
 * */
@MsgPack
case class DealCards(
    _type: model.Dealer.DealType,
    var cards: List[poker.Card] = List.empty,
    @BeanProperty pos: Integer = null,
    var player: model.Player = null,
    @BeanProperty cardsNum: Option[Int] = None) extends DealEventBase with HasPlayer with HasCards {
  def this() = this(null)
  @BeanProperty var eType = DealEventType.DEAL_CARDS
}

@MsgPack
case class RequireBet(
    @BeanProperty var pos: Integer,
    var player: model.Player,
    var call: Decimal,
    var raise: model.Range) extends DealEventBase with HasPlayer {
  def this() = this(null, null, .0, model.Range(.0, .0))
  @BeanProperty var eType = DealEventType.REQUIRE_BET
  def getCall: Double = call.toDouble
  def setCall(v: Double) = call = v
  
  def getRaise: Range = new Range(raise.min.toDouble, raise.max.toDouble)
  def setRaise(r: Range) = raise = model.Range(r.min, r.max)
}

@MsgPack
case class RequireDiscard(
    @BeanProperty var pos: Int,
    var player: model.Player) extends DealEventBase with HasPlayer {
  def this() = this(0, null)
  @BeanProperty var eType = DealEventType.REQUIRE_DISCARD
}

@MsgPack
case class DeclarePot(
    var pot: Decimal,
    var rake: Option[Decimal] = None) extends DealEventBase {
  def this() = this(.0)
  
  @BeanProperty var eType = DealEventType.DECLARE_POT
  
  def getPot: Double = pot.toDouble
  def setPot(v: Double) = pot = v
  
  def getRake: Double = rake.map(_.toDouble).getOrElse(.0)
  def setRake(v: Double) = rake = Some(v)
}

@MsgPack
case class DeclareHand(
    @BeanProperty var pos: Int,
    var player: model.Player,
    var cards: List[poker.Card],
    hand: poker.Hand) extends DealEventBase with HasPlayer with HasCards {
  def this() = this(0, null, List.empty, null)
  @BeanProperty var eType = DealEventType.DECLARE_HAND
  def getHand: Hand = new Hand // FIXME
  def setHand(h: Hand) = {} // FIXME
}

@MsgPack
case class DeclareWinner(
    @BeanProperty var pos: Int,
    var player: model.Player,
    var amount: Decimal) extends DealEventBase with HasPlayer with HasAmount {
  def this() = this(0, null, .0)
  @BeanProperty var eType = DealEventType.DECLARE_WINNER
}

/**
 * Command
 * */
@MsgPack
case class JoinTable(
    @BeanProperty var pos: Integer,
    var amount: Decimal,
    var player: model.Player) extends CmdBase with HasPlayer with HasAmount {
  def this() = this(null, .0, null)
  @BeanProperty var cType = CmdType.JOIN_TABLE
}
/**
 * Msg
 * */
case class Chat(
    @BeanProperty var body: String) extends MsgBase {
  def this() = this("")
  @BeanProperty var mType: MsgType = MsgType.CHAT
}

case class Dealer(
    @BeanProperty var body: String) extends MsgBase {
  def this() = this("")
  @BeanProperty var mType: MsgType = MsgType.DEALER
}

case class Error(
    @BeanProperty var body: String) extends MsgBase {
  def this() = this("")
  @BeanProperty var mType: MsgType = MsgType.ERROR
}
