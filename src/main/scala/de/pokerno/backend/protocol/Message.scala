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
sealed case class AddBet(
    @BeanProperty var pos: Integer,
    var player: model.Player,
    var bet: model.Bet) extends ActionEventBase with HasPlayer {
  def this() = this(0, null, null)
  override def getBet: Bet = new Bet()
  override def setBet(v: Bet) = {}
  @BeanProperty var `type` = ActionEventType.ADD_BET
}

@MsgPack
sealed case class DiscardCards(
    @BeanProperty var pos: Integer,
    var player: model.Player,
    var cards: List[poker.Card]) extends ActionEventBase with HasPlayer with HasCards {
  def this() = this(null, null, null)
  @BeanProperty var `type` = ActionEventType.DISCARD_CARDS
}

/**
 * Table event
 * */
@MsgPack
sealed case class ButtonChange(
    @BeanProperty var button: Int) extends TableEventBase {
  def this() = this(0)
  @BeanProperty var `type` = TableEventType.BUTTON
}
/**
 * Gameplay event
 * */
@MsgPack
sealed case class GameChange(
    game: model.Game) extends GameplayEventBase {
  def this() = this(null)
  @BeanProperty var `type` = GameplayEventType.GAME
}

@MsgPack
sealed case class StakeChange(
    stake: model.Game) extends GameplayEventBase {
  def this() = this(null)
  @BeanProperty var `type` = GameplayEventType.STAKE
}

/**
 * Stage event
 * */
@MsgPack
sealed case class PlayStart(
    game: model.Game,
    stake: model.Stake) extends StageEventBase {
  def this() = this(null, null)
  @BeanProperty var stage = StageType.PLAY
  @BeanProperty var `type` = StageEventType.START
}

@MsgPack
sealed case class PlayStop() extends StageEventBase {
  @BeanProperty var stage = StageType.PLAY
  @BeanProperty var `type` = StageEventType.STOP
  //def this() = this()
}

@MsgPack
sealed case class StreetStart(name: String) extends StageEventBase {
  @BeanProperty var stage = StageType.STREET
  @BeanProperty var `type` = StageEventType.START
  def this() = this("")
}

/**
 * Deal event
 * */
@MsgPack
sealed case class DealCards(
    dealType: model.Dealer.DealType,
    var cards: List[poker.Card] = List.empty,
    @BeanProperty var pos: Integer = null,
    var player: model.Player = null,
    @BeanProperty var cardsNum: Integer = null) extends DealEventBase with HasPlayer with HasCards {
  def this() = this(null)
  @BeanProperty var `type` = DealEventType.DEAL_CARDS
  
  def getDealType: DealerDealType = null // FIXME
  def setDealType(v: DealerDealType) = {} // FIXME
}

@MsgPack
sealed case class RequireBet(
    @BeanProperty var pos: Integer,
    var player: model.Player,
    var call: Decimal,
    var raise: model.Range) extends DealEventBase with HasPlayer {
  def this() = this(null, null, .0, model.Range(.0, .0))
  @BeanProperty var `type` = DealEventType.REQUIRE_BET
  def getCall: Double = call.toDouble
  def setCall(v: Double) = call = v
  
  def getRaise: Range = new Range(raise.min.toDouble, raise.max.toDouble)
  def setRaise(r: Range) = raise = model.Range(r.min, r.max)
}

@MsgPack
sealed case class RequireDiscard(
    @BeanProperty var pos: Int,
    var player: model.Player) extends DealEventBase with HasPlayer {
  def this() = this(0, null)
  @BeanProperty var `type` = DealEventType.REQUIRE_DISCARD
}

@MsgPack
sealed case class DeclarePot(
    var pot: Decimal,
    var rake: Option[Decimal] = None) extends DealEventBase {
  def this() = this(.0)
  
  @BeanProperty var `type` = DealEventType.DECLARE_POT
  
  def getPot: java.lang.Double = pot.toDouble
  def setPot(v: java.lang.Double) = pot = v.toDouble
  
  def getRake: java.lang.Double = rake match {
    case Some(v) => v.toDouble
    case _ => null
  }
  
  def setRake(v: java.lang.Double) = rake = Some(v.toDouble)
}

@MsgPack
sealed case class DeclareHand(
    @BeanProperty var pos: Int,
    var player: model.Player,
    var cards: List[poker.Card],
    hand: poker.Hand) extends DealEventBase with HasPlayer with HasCards {
  def this() = this(0, null, List.empty, null)
  @BeanProperty var `type` = DealEventType.DECLARE_HAND
  def getHand: Hand = new Hand // FIXME
  def setHand(h: Hand) = {} // FIXME
}

@MsgPack
sealed case class DeclareWinner(
    @BeanProperty var pos: Int,
    var player: model.Player,
    var amount: Decimal) extends DealEventBase with HasPlayer with HasAmount {
  def this() = this(0, null, .0)
  @BeanProperty var `type` = DealEventType.DECLARE_WINNER
}

/**
 * Command
 * */
@MsgPack
sealed case class JoinTable(
    @BeanProperty var pos: Integer,
    var amount: Decimal,
    var player: model.Player) extends CmdBase with HasPlayer with HasAmount {
  def this() = this(null, .0, null)
  @BeanProperty var `type` = CmdType.JOIN_TABLE
}
/**
 * Msg
 * */
sealed case class Chat(
    @BeanProperty var body: String) extends MsgBase {
  def this() = this("")
  @BeanProperty var `type`: MsgType = MsgType.CHAT
}

sealed case class Dealer(
    @BeanProperty var body: String) extends MsgBase {
  def this() = this("")
  @BeanProperty var `type`: MsgType = MsgType.DEALER
}

sealed case class Error(
    @BeanProperty var body: String) extends MsgBase {
  def this() = this("")
  @BeanProperty var `type`: MsgType = MsgType.ERROR
}
