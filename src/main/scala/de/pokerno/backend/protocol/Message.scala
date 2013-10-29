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
  @BeanProperty var eType = ActionEventType.AddBet
}

@MsgPack
case class DiscardCards(
    @BeanProperty var pos: Int,
    var player: model.Player,
    var cards: List[poker.Card]) extends ActionEventBase with HasPlayer with HasCards {
  def this() = this(0, null, null)
  @BeanProperty var eType = ActionEventType.DiscardCards
}

/**
 * Table event
 * */
@MsgPack
case class ButtonChange(
    @BeanProperty button: Int) extends TableEvent {
  def this() = this(0)
  @BeanProperty var eType = TableEventType.ButtonChange
}
/**
 * Gameplay event
 * */
@MsgPack
case class GameChange(
    game: model.Game) extends GameplayEvent {
  def this() = this(null)
  @BeanProperty var eType = GameplayEventType.GameChange
}

@MsgPack
case class StakeChange(
    stake: model.Game) extends GameplayEvent {
  def this() = this(null)
  @BeanProperty var eType = GameplayEventType.StakeChange
}

/**
 * Stage event
 * */
@MsgPack
case class PlayStart(
    game: model.Game,
    stake: model.Stake) extends StageEvent {
  def this() = this(null, null)
  @BeanProperty var stage = StageType.Play
  @BeanProperty var eType = StageEventType.Start
}

@MsgPack
case class PlayStop() extends StageEvent {
  @BeanProperty var stage = StageType.Play
  @BeanProperty var eType = StageEventType.Start
  //def this() = this()
}

@MsgPack
case class StreetStart(name: String) extends StageEvent {
  @BeanProperty var stage = StageType.Street
  @BeanProperty var eType = StageEventType.Start
  def this() = this("")
}

/**
 * Deal event
 * */
@MsgPack
case class DealCards(
    _type: model.Dealer.DealType,
    var cards: List[poker.Card] = List.empty,
    @BeanProperty pos: Option[Int] = None,
    var player: Option[model.Player] = None,
    @BeanProperty cardsNum: Option[Int] = None) extends DealEvent with HasPlayer with HasCards {
  def this() = this(null)
  @BeanProperty var eType = DealEventType.Deal
}

@MsgPack
case class RequireBet(
    @BeanProperty var pos: Int,
    var player: model.Player,
    call: Decimal,
    raise: model.Range) extends DealEvent with HasPlayer {
  def this() = this(0, null, .0, model.Range(.0, .0))
  @BeanProperty var eType = DealEventType.AskBet
}

@MsgPack
case class RequireDiscard(
    @BeanProperty var pos: Int,
    var player: model.Player) extends DealEvent with HasPlayer {
  def this() = this(0, null)
  @BeanProperty var eType = DealEventType.AskDiscard
}

@MsgPack
case class DeclarePot(
    var pot: Decimal,
    var rake: Option[Decimal] = None) extends DealEvent {
  def this() = this(.0)
  
  @BeanProperty var eType = DealEventType.ShowPot
  
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
    hand: poker.Hand) extends DealEvent with HasPlayer with HasCards {
  def this() = this(0, null, List.empty, null)
  @BeanProperty var eType = DealEventType.ShowHand
}

@MsgPack
case class DeclareWinner(
    @BeanProperty pos: Int,
    var player: model.Player,
    var amount: Decimal) extends DealEvent with HasPlayer with HasAmount {
  def this() = this(0, null, .0)
  @BeanProperty var eType = DealEventType.ShowWinner
}

/**
 * Command
 * */
@MsgPack
case class JoinTable(
    @BeanProperty var pos: Int,
    var amount: Decimal,
    var player: model.Player) extends Cmd with HasPlayer with HasAmount {
  def this() = this(0, .0, null)
  @BeanProperty var cType = CmdType.Join
}
/**
 * Msg
 * */
case class Chat(
    @BeanProperty var body: String) extends Msg {
  def this() = this("")
  @BeanProperty var mType: MsgType = MsgType.Chat
}
