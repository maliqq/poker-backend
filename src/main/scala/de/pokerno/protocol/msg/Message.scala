package de.pokerno.protocol.msg

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.poker
import de.pokerno.model
import de.pokerno.gameplay
import de.pokerno.protocol.{wire, Message => BaseMessage, HasPlayer, HasCards, HasAmount}
import wire.Conversions._
import Conversions._

import com.dyuproject.protostuff
import beans._
import org.msgpack.annotation.{ Message => MsgPack }
import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonSubTypes}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "$type"
)
@JsonSubTypes(Array(
  new JsonSubTypes.Type(value = classOf[AddBet], name="AddBet"),
  new JsonSubTypes.Type(value = classOf[DiscardCards], name="DiscardCards"),
  new JsonSubTypes.Type(value = classOf[ShowCards], name="ShowCards"),
  new JsonSubTypes.Type(value = classOf[ButtonChange], name="ButtonChange"),
  new JsonSubTypes.Type(value = classOf[GameChange], name="GameChange"),
  new JsonSubTypes.Type(value = classOf[StakeChange], name="StakeChange"),
  new JsonSubTypes.Type(value = classOf[PlayStart], name="PlayStart"),
  new JsonSubTypes.Type(value = classOf[PlayStop], name="PlayStop"),
  new JsonSubTypes.Type(value = classOf[StreetStart], name="StreetStart"),
  new JsonSubTypes.Type(value = classOf[DealCards], name="DealCards"),
  new JsonSubTypes.Type(value = classOf[RequireBet], name="RequireBet"),
  new JsonSubTypes.Type(value = classOf[RequireDiscard], name="RequireDiscard"),
  new JsonSubTypes.Type(value = classOf[DeclarePot], name="DeclarePot"),
  new JsonSubTypes.Type(value = classOf[DeclareHand], name="DeclareHand"),
  new JsonSubTypes.Type(value = classOf[DeclareWinner], name="DeclareWinner"),
  new JsonSubTypes.Type(value = classOf[JoinTable], name="JoinTable"),
  new JsonSubTypes.Type(value = classOf[Chat], name="Chat"),
  new JsonSubTypes.Type(value = classOf[Dealer], name="Dealer"),
  new JsonSubTypes.Type(value = classOf[Error], name="Error")
))
abstract class Message extends BaseMessage

/**
 * Action event
 * */
@MsgPack
sealed case class AddBet(var _bet: model.Bet) extends Message {
  def schema = AddBetSchema.SCHEMA
  
  def this() = this(null)

  def bet = _bet
  def getBet: wire.Bet = if (_bet != null) wire.Bet(_bet.betType, _bet.amount.toDouble)
    else null
  def setBet(v: wire.Bet) = _bet = new model.Bet(v.getType, v.getAmount.toDouble)
  
}

object AddBet {

  def apply(pos: Integer, player: model.Player, bet: model.Bet): ActionEvent = new ActionEvent {
    `type` = ActionEventSchema.EventType.ADD_BET
    this.pos = pos
    this.player = player
    addBet = AddBet(bet)
  }
  
  def unapply(e: ActionEvent) = if (e.`type` == ActionEventSchema.EventType.ADD_BET)
    Some((e.pos, e.player, e.addBet.bet))
  else
    None

}

@MsgPack
sealed case class DiscardCards(var cards: List[poker.Card]) extends Message with HasCards {
  def schema = DiscardCardsSchema.SCHEMA
  
  @BeanProperty
  var cardsNum: Integer = null
  
  @BeanProperty
  var `type`: DiscardCardsSchema.DiscardType = null

  def this() = this(null)
  
}

object DiscardCards {
  
  def apply(pos: Integer, player: model.Player, cards: List[poker.Card]): ActionEvent = new ActionEvent {
    `type` = ActionEventSchema.EventType.DISCARD_CARDS
    this.pos = pos
    this.player = player
    discardCards = DiscardCards(cards)
  }

}

@MsgPack
sealed case class ShowCards(
    var cards: List[poker.Card],
    @BeanProperty
    var muck: java.lang.Boolean = null) extends Message with HasCards {
  
  def schema = ShowCardsSchema.SCHEMA
  
  @BeanProperty
  var `type`: ShowCardsSchema.ShowType = if (muck)
    ShowCardsSchema.ShowType.MUCK
  else
    ShowCardsSchema.ShowType.SHOW

  def this() = this(null)
  
}

object ShowCards {
  def apply(pos: Integer, player: model.Player, cards: List[poker.Card]): ActionEvent = apply(pos, player, cards, false)
  
  def apply(pos: Integer, player: model.Player, cards: List[poker.Card], muck: Boolean): ActionEvent = new ActionEvent {
    `type` = ActionEventSchema.EventType.SHOW_CARDS
    this.pos = pos
    this.player = player
    showCards = ShowCards(cards, muck)
  }

}

/**
 * Table event
 * */
@MsgPack
sealed case class ButtonChange(_button: Integer) extends TableEvent {
  
  setButton(_button)
  `type` = TableEventSchema.EventType.BUTTON
  
  def this() = this(null)
  
}

/**
 * Gameplay event
 * */
@MsgPack
sealed case class GameChange(
    _game: model.Game) extends GameplayEvent {
  
  `type` = GameplayEventSchema.EventType.GAME
  def this() = this(null)
  
  override def getGame: wire.Game = new wire.Game
  override def setGame(g: wire.Game) = {}
  
}

@MsgPack
sealed case class StakeChange(_stake: model.Stake) extends GameplayEvent {
  
  `type` = GameplayEventSchema.EventType.STAKE
  def this() = this(null)
  
  override def getStake: wire.Stake = new wire.Stake
  override def setStake(s: wire.Stake) = {}
  
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
    
    @BeanProperty
    var `type`: model.DealCards.Value,
    
    var cards: List[poker.Card] = List.empty,
    
    @BeanProperty
    var pos: Integer = null,
    
    var player: model.Player = null,
    
    @BeanProperty
    var cardsNum: Integer = null) extends Message with HasPlayer with HasCards {

  def this() = this(null)
  
  def schema = DealCardsSchema.SCHEMA
  //def pipeSchema = DealCardsSchema.PIPE_SCHEMA
  
}

@MsgPack
sealed case class RequireBet(
    @BeanProperty
    var pos: Integer,
    
    var player: model.Player,
    
    var call: Decimal,
    
    var raise: model.Range) extends Message with HasPlayer{
  
  def schema = RequireBetSchema.SCHEMA
  //def pipeSchema = RequireBetSchema.PIPE_SCHEMA
  
  def this() = this(null, null, null, model.Range(.0, .0))
  @BeanProperty
  var `type` = DealEventSchema.EventType.REQUIRE_BET
  def getCall: Double = call.toDouble
  def setCall(v: Double) = call = v
  
  def getRaise: wire.Range = new wire.Range(raise.min.toDouble, raise.max.toDouble)
  def setRaise(r: wire.Range) = raise = model.Range(r.min.toDouble, r.max.toDouble)
  
}

@MsgPack
sealed case class RequireDiscard(
    @BeanProperty
    var pos: Integer,
    
    var player: model.Player) extends Message with HasPlayer {
  
  def schema = RequireDiscardSchema.SCHEMA
  //def pipeSchema = RequireDiscardSchema.PIPE_SCHEMA
  def this() = this(null, null)
  
}

@MsgPack
sealed case class DeclarePot(
    var pot: Decimal,
    var rake: Option[Decimal] = None) extends Message {
  
  def schema = DeclarePotSchema.SCHEMA
  //def pipeSchema = DeclarePotSchema.PIPE_SCHEMA
  
  def this() = this(null)
  
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
    @BeanProperty
    var pos: Integer,
    var player: model.Player,
    var cards: List[poker.Card],
    var hand: poker.Hand) extends Message with HasPlayer with HasCards {
  
  def schema = DeclareHandSchema.SCHEMA
  //def pipeSchema = DeclareHandSchema.PIPE_SCHEMA
  def this() = this(null, null, List.empty, null)
  
  def getHand: wire.Hand = new wire.Hand(
      cards = hand.cards.value,
      rank = hand.rank.get,
      high = hand.high,
      value = hand.value,
      kicker = hand.kicker,
      string = hand.description
  )
  
  def setHand(h: wire.Hand) {
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
    @BeanProperty
    var pos: Integer,
    var player: model.Player,
    var amount: Decimal) extends Message with HasPlayer with HasAmount {
  
  def schema = DeclareWinnerSchema.SCHEMA
  //def pipeSchema = DeclareWinnerSchema.PIPE_SCHEMA
  def this() = this(null, null, null)
  
}

@MsgPack
sealed case class TickTimer(
    @BeanProperty
    var pos: Integer,
    var player: model.Player,
    @BeanProperty
    var timeLeft: Integer,
    @BeanProperty
  var timeBank: java.lang.Boolean = false
) extends Message with HasPlayer {
  
  def this() = this(null, null, null)
  def schema = TickTimerSchema.SCHEMA
  def isTimeBank = timeBank
  
}

/**
 * Command
 * */
@MsgPack
sealed case class JoinTable(
    @BeanProperty
    var pos: Integer,
    var player: model.Player,
    var amount: Decimal) extends Message with HasAmount with HasPlayer {
  
  def schema = JoinTableSchema.SCHEMA
  //def pipeSchema = JoinTableSchema.PIPE_SCHEMA
  
  def this() = this(null, null, null)
}

/**
 * Msg
 * */
sealed case class Chat(_body: String) extends Msg {
  body = _body
  `type` = MsgSchema.MsgType.CHAT
  
  def this() = this(null)
  
}

sealed case class Dealer(_body: String) extends Msg {
  body = _body
  `type` = MsgSchema.MsgType.DEALER
  
  def this() = this(null)
  
}

sealed case class Error(_body: String) extends Msg {
  body = _body
  `type` = MsgSchema.MsgType.ERROR
  
  def this() = this(null)
  
}
