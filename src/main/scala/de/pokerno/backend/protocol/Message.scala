package de.pokerno.backend.protocol

import scala.math.{ BigDecimal ⇒ Decimal }
import de.pokerno.poker
import de.pokerno.model
import scala.reflect._
import org.msgpack.annotation.{ Message => MsgPack }

trait Message {
}
/**
 * Action event
 * */
@MsgPack
sealed case class AddBet(
    _pos: Integer,
    _player: model.Player,
    _bet: model.Bet) extends ActionEvent with HasPlayer {
  setPos(_pos)
  `type` = ActionEventSchema.EventType.ADD_BET
  def this() = this(0, null, null)
  override def getBet: Bet = new Bet()
  override def setBet(v: Bet) = {}
}

@MsgPack
sealed case class DiscardCards(
    _pos: Integer,
    _player: model.Player,
    _cards: List[poker.Card]) extends ActionEvent with HasPlayer with HasCards {
  setPos(_pos)
  `type` = ActionEventSchema.EventType.DISCARD_CARDS
  def this() = this(null, null, null)
}

/**
 * Table event
 * */
@MsgPack
sealed case class ButtonChange(
    _button: Integer) extends TableEvent {
  setButton(_button)
  `type` = TableEventSchema.EventType.BUTTON
  def this() = this(0)
}
/**
 * Gameplay event
 * */
@MsgPack
sealed case class GameChange(
    _game: model.Game) extends GameplayEvent {
  `type` = GameplayEventSchema.EventType.GAME
  def this() = this(null)
  
  override def getGame: Game = new Game
  override def setGame(g: Game) = {}
}

@MsgPack
sealed case class StakeChange(
    _stake: model.Stake) extends GameplayEvent {
  `type` = GameplayEventSchema.EventType.STAKE
  def this() = this(null)

  override def getStake: Stake = new Stake
  override def setStake(s: Stake) = {}
}

/**
 * Stage event
 * */
@MsgPack
sealed case class PlayStart() extends StageEvent {
  stage = StageEventSchema.StageType.PLAY
  `type` = StageEventSchema.EventType.START
}

@MsgPack
sealed case class PlayStop() extends StageEvent {
  stage = StageEventSchema.StageType.PLAY
  `type` = StageEventSchema.EventType.STOP
  //def this() = this()
}

@MsgPack
sealed case class StreetStart(name: String) extends StageEvent {
  stage = StageEventSchema.StageType.STREET
  `type` = StageEventSchema.EventType.START
  def this() = this("")
}

/**
 * Deal event
 * */
trait DealEventBase extends Message
@MsgPack
sealed case class DealCards(
    dealType: model.Dealer.DealType,
    var cards: List[poker.Card] = List.empty,
    @BeanProperty var pos: Integer = null,
    var player: model.Player = null,
    @BeanProperty var cardsNum: Integer = null) extends DealEventBase with HasPlayer with HasCards {
  def this() = this(null)
  @BeanProperty var `type` = DealEventSchema.EventType.DEAL_CARDS
  
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
  @BeanProperty var `type` = DealEventSchema.EventType.REQUIRE_BET
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
  @BeanProperty var `type` = DealEventSchema.EventType.REQUIRE_DISCARD
}

@MsgPack
sealed case class DeclarePot(
    var pot: Decimal,
    var rake: Option[Decimal] = None) extends DealEventBase {
  def this() = this(.0)
  
  @BeanProperty var `type` = DealEventSchema.EventType.DECLARE_POT
  
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
  @BeanProperty var `type` = DealEventSchema.EventType.DECLARE_HAND
  def getHand: Hand = new Hand // FIXME
  def setHand(h: Hand) = {} // FIXME
}

@MsgPack
sealed case class DeclareWinner(
    @BeanProperty var pos: Int,
    var player: model.Player,
    var amount: Decimal) extends DealEventBase with HasPlayer with HasAmount {
  @BeanProperty var `type` = DealEventSchema.EventType.DECLARE_WINNER
  def this() = this(0, null, .0)
}

/**
 * Command
 * */
@MsgPack
sealed case class JoinTable(
    @BeanProperty var pos: Integer,
    var amount: Decimal,
    var player: model.Player) extends Cmd with HasPlayer with HasAmount {
  `type` = CmdSchema.CmdType.JOIN_TABLE
  def this() = this(null, .0, null)
}
/**
 * Msg
 * */
sealed case class Chat(_body: String) extends Msg {
  body = _body
  `type` = MsgSchema.MsgType.CHAT
  def this() = this("")
}

sealed case class Dealer(_body: String) extends Msg {
  body = _body
  `type` = MsgSchema.MsgType.DEALER
  def this() = this("")
}

sealed case class Error(_body: String) extends Msg {
  body = _body
  `type` = MsgSchema.MsgType.ERROR
  def this() = this("")
}
