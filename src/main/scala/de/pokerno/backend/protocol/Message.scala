package de.pokerno.backend.protocol

import scala.math.{ BigDecimal ⇒ Decimal }
import de.pokerno.poker
import de.pokerno.model
import de.pokerno.gameplay
import scala.reflect._
import org.msgpack.annotation.{ Message => MsgPack }
import com.dyuproject.protostuff

trait Message {
  def schema: protostuff.Schema[Any]
  //def pipeSchema: protostuff.Pipe.Schema[_]
  
  def getSchema = schema
  //def getPipeSchema = pipeSchema
  
  def messageName: String = getClass.getSimpleName
}

/**
 * Action event
 * */
@MsgPack
sealed case class AddBet(
    @BeanProperty var pos: Integer,
    var player: model.Player,
    var _bet: model.Bet) extends Message with HasPlayer {
  def schema = AddBetSchema.SCHEMA.asInstanceOf[protostuff.Schema[Any]]
  //def pipeSchema = AddBetSchema.PIPE_SCHEMA
  def this() = this(0, null, null)

  def getBet: Bet = if (_bet != null) Bet(_bet.betType, _bet.amount.toDouble)
    else null
  def setBet(v: Bet) = _bet = new model.Bet(v.getType, v.getAmount.toDouble)
}

@MsgPack
sealed case class DiscardCards(
    @BeanProperty var pos: Integer,
    var player: model.Player,
    var cards: List[poker.Card]) extends Message with HasPlayer with HasCards {
  def schema = DiscardCardsSchema.SCHEMA.asInstanceOf[protostuff.Schema[Any]]
  //def pipeSchema = DiscardCardsSchema.PIPE_SCHEMA
  @BeanProperty var cardsNum: Integer = null
  @BeanProperty var `type`: DiscardCardsSchema.DiscardType = null
  def this() = this(null, null, null)
}

@MsgPack
sealed case class ShowCards(
    @BeanProperty var pos: Integer,
    var player: model.Player,
    var cards: List[poker.Card],
    muck: Boolean = false) extends Message with HasPlayer with HasCards {
  def schema = ShowCardsSchema.SCHEMA.asInstanceOf[protostuff.Schema[Any]]
  //def pipeSchema = ShowCardsSchema.PIPE_SCHEMA
  @BeanProperty var `type`: ShowCardsSchema.ShowType = if (muck)
      ShowCardsSchema.ShowType.MUCK
    else
      ShowCardsSchema.ShowType.SHOW
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
sealed case class StreetStart(streetName: gameplay.Street.Value) extends StageEvent {
  stage = StageEventSchema.StageType.STREET
  `type` = StageEventSchema.EventType.START
  street = streetName
  def this() = this(null)
}

/**
 * Deal event
 * */

@MsgPack
sealed case class DealCards(
    @BeanProperty var `type`: model.DealCards.Value,
    var cards: List[poker.Card] = List.empty,
    @BeanProperty var pos: Integer = null,
    var player: model.Player = null,
    @BeanProperty var cardsNum: Integer = null) extends Message with HasPlayer with HasCards {

  def this() = this(null)
  
  def schema = DealCardsSchema.SCHEMA.asInstanceOf[protostuff.Schema[Any]]
  //def pipeSchema = DealCardsSchema.PIPE_SCHEMA
}

@MsgPack
sealed case class RequireBet(
    @BeanProperty var pos: Integer,
    var player: model.Player,
    var call: Decimal,
    var raise: model.Range) extends Message with HasPlayer{
  def schema = RequireBetSchema.SCHEMA.asInstanceOf[protostuff.Schema[Any]]
  //def pipeSchema = RequireBetSchema.PIPE_SCHEMA
  
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
    var player: model.Player) extends Message {
  def schema = RequireDiscardSchema.SCHEMA.asInstanceOf[protostuff.Schema[Any]]
  //def pipeSchema = RequireDiscardSchema.PIPE_SCHEMA
  def this() = this(0, null)
  
  def getPlayer: String = player.toString
  def setPlayer(v: String) = player = new model.Player(v)
}

@MsgPack
sealed case class DeclarePot(
    var pot: Decimal,
    var rake: Option[Decimal] = None) extends Message {
  def schema = DeclarePotSchema.SCHEMA.asInstanceOf[protostuff.Schema[Any]]
  //def pipeSchema = DeclarePotSchema.PIPE_SCHEMA
  def this() = this(.0)
  
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
    var hand: poker.Hand) extends Message with HasPlayer with HasCards {
  def schema = DeclareHandSchema.SCHEMA.asInstanceOf[protostuff.Schema[Any]]
  //def pipeSchema = DeclareHandSchema.PIPE_SCHEMA
  def this() = this(0, null, List.empty, null)
  
  def getHand: Hand = new Hand(
      cards = hand.cards.value,
      rank = hand.rank.get,
      high = hand.high,
      value = hand.value,
      kicker = hand.kicker,
      string = hand.description
  )
  
  def setHand(h: Hand) {
    hand = new poker.Hand(
      cards = new poker.Hand.Cards(h.cards),
      rank = Some(h.rank),
      value = h.value,
      High = Left(h.high),
      Kicker = Left(h.kicker)
    )
  }
}

@MsgPack
sealed case class DeclareWinner(
    @BeanProperty var pos: Int,
    var player: model.Player,
    var amount: Decimal) extends Message with HasPlayer with HasAmount {
  def schema = DeclareWinnerSchema.SCHEMA.asInstanceOf[protostuff.Schema[Any]]
  //def pipeSchema = DeclareWinnerSchema.PIPE_SCHEMA
  def this() = this(0, null, .0)
}

/**
 * Command
 * */
@MsgPack
sealed case class JoinTable(
    @BeanProperty var pos: Integer,
    var amount: Decimal,
    var player: model.Player) extends Message with HasAmount {
  def schema = JoinTableSchema.SCHEMA.asInstanceOf[protostuff.Schema[Any]]
  //def pipeSchema = JoinTableSchema.PIPE_SCHEMA
  def this() = this(null, .0, null)
  
  def getPlayer: String = player.toString
  def setPlayer(v: String) = player = new model.Player(v)
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
