package de.pokerno.protocol.msg

import de.pokerno.protocol.{wire, Message => BaseMessage}

import com.dyuproject.protostuff
import com.dyuproject.protostuff.ByteString
import beans._
import org.msgpack.annotation.{ Message => MsgPack }
import com.fasterxml.jackson.annotation.{JsonInclude, JsonTypeInfo, JsonSubTypes}

@JsonSubTypes(Array(
  new JsonSubTypes.Type(value = classOf[BetAdd], name="AddBet"),
  new JsonSubTypes.Type(value = classOf[CardsDiscard], name="DiscardCards"),
  new JsonSubTypes.Type(value = classOf[CardsShow], name="ShowCards"),
  
  new JsonSubTypes.Type(value = classOf[DealCards], name="DealCards"),
  new JsonSubTypes.Type(value = classOf[RequireBet], name="RequireBet"),
  new JsonSubTypes.Type(value = classOf[RequireDiscard], name="RequireDiscard"),
  new JsonSubTypes.Type(value = classOf[DeclarePot], name="DeclarePot"),
  new JsonSubTypes.Type(value = classOf[DeclareHand], name="DeclareHand"),
  new JsonSubTypes.Type(value = classOf[DeclareWinner], name="DeclareWinner"),
  new JsonSubTypes.Type(value = classOf[TickTimer], name="TickTimer"),

//  new JsonSubTypes.Type(value = classOf[StageEvent], name="StageEvent"),
//  new JsonSubTypes.Type(value = classOf[GameplayEvent], name="GameplayEvent"),
//  new JsonSubTypes.Type(value = classOf[DealEvent], name="DealEvent"),
  new JsonSubTypes.Type(value = classOf[Start], name="Start"),
  new JsonSubTypes.Type(value = classOf[ButtonChange], name="ButtonChange"),
  new JsonSubTypes.Type(value = classOf[GameChange], name="GameChange"),
  new JsonSubTypes.Type(value = classOf[StakeChange], name="StakeChange"),
  new JsonSubTypes.Type(value = classOf[PlayStart], name="PlayStart"),
  new JsonSubTypes.Type(value = classOf[PlayStop], name="PlayStop"),
  new JsonSubTypes.Type(value = classOf[StreetStart], name="StreetStart"),
  new JsonSubTypes.Type(value = classOf[PlayerJoin], name="JoinTable"),
  new JsonSubTypes.Type(value = classOf[Chat], name="Chat"),
  new JsonSubTypes.Type(value = classOf[Dealer], name="Dealer"),
  new JsonSubTypes.Type(value = classOf[Error], name="Error")
))
@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class Outbound extends Message

@MsgPack
sealed case class BetAdd(
    @BeanProperty
    var pos: Integer,
    
    @BeanProperty
    var player: String,

    @BeanProperty
    var bet: wire.Bet) extends Outbound {
  def schema = BetAddSchema.SCHEMA
  def this() = this(null, null, null)
}

@MsgPack
sealed case class CardsDiscard(
    @BeanProperty
    var pos: Integer,
    
    @BeanProperty
    var player: String,
    
    @BeanProperty
    var cards: ByteString,

    @BeanProperty
    var cardsNum: Integer = null) extends Outbound {
  def schema = CardsDiscardSchema.SCHEMA
  def this() = this(null, null, null)
}

@MsgPack
sealed case class CardsShow(
    @BeanProperty
    var pos: Integer,

    @BeanProperty
    var player: String,

    @BeanProperty
    var cards: ByteString,
    
    @BeanProperty
    var muck: java.lang.Boolean = null) extends Outbound {
  def schema = CardsShowSchema.SCHEMA
  def this() = this(null, null, null)
  def isMuck = getMuck
}
/**
 * Table event
 * */
@MsgPack
sealed case class ButtonChange(
  _button: Integer

) extends TableEvent(TableEventSchema.EventType.BUTTON) {
  
  setButton(_button)

  def this() = this(null)
  
}

/**
 * Gameplay event
 * */
@MsgPack
sealed case class GameChange(
    _game: wire.Game) extends GameplayEvent(GameplayEventSchema.EventType.GAME) {
  
  setGame(_game)

  def this() = this(null)
  
}

@MsgPack
sealed case class StakeChange(
  _stake: wire.Stake) extends GameplayEvent(GameplayEventSchema.EventType.STAKE) {

  setStake(_stake)

  def this() = this(null)
}

/**
 * Stage event
 * */
@MsgPack
sealed case class PlayStart() extends StageEvent(StageEventSchema.EventType.START) {
  
  stage = StageEventSchema.StageType.PLAY
  
}

@MsgPack
sealed case class PlayStop() extends StageEvent(StageEventSchema.EventType.STOP) {
  
  stage = StageEventSchema.StageType.PLAY
  //def this() = this()
  
}

@MsgPack
sealed case class StreetStart(streetName: StageEventSchema.StreetType) extends StageEvent(StageEventSchema.EventType.START) {
  
  stage = StageEventSchema.StageType.STREET
  street = streetName
  def this() = this(null)
  
}

/**
 * Deal event
 * */

@MsgPack
sealed case class DealCards(
    
    @BeanProperty
    var `type`: wire.DealType,
    
    @BeanProperty
    var cards: ByteString = null,
    
    @BeanProperty
    var pos: Integer = null,
    
    @BeanProperty
    var player: String = null,
    
    @BeanProperty
    var cardsNum: Integer = null) extends Message {

  def this() = this(null)
  
  def schema = DealCardsSchema.SCHEMA
  //def pipeSchema = DealCardsSchema.PIPE_SCHEMA
  
}

@MsgPack
sealed case class RequireBet(
    @BeanProperty
    var pos: Integer,
    
    @BeanProperty
    var player: String,
    
    @BeanProperty
    var call: java.lang.Double,
    
    @BeanProperty
    var raise: wire.Range) extends Message {
  
  def schema = RequireBetSchema.SCHEMA
  //def pipeSchema = RequireBetSchema.PIPE_SCHEMA
  
  def this() = this(null, null, null, wire.Range(.0, .0))
  //@BeanProperty
  //var `type` = DealEventSchema.EventType.REQUIRE_BET
  
}

@MsgPack
sealed case class RequireDiscard(
    @BeanProperty
    var pos: Integer,
    
    @BeanProperty
    var player: String) extends Message {
  
  def schema = RequireDiscardSchema.SCHEMA
  //def pipeSchema = RequireDiscardSchema.PIPE_SCHEMA
  def this() = this(null, null)
  
}

@MsgPack
sealed case class DeclarePot(
    @BeanProperty
    var pot: java.lang.Double,

    @BeanProperty
    var rake: java.lang.Double = null) extends Message {
  
  def schema = DeclarePotSchema.SCHEMA
  //def pipeSchema = DeclarePotSchema.PIPE_SCHEMA
  
  def this() = this(null)  
}

@MsgPack
sealed case class DeclareHand(
    @BeanProperty
    var pos: Integer,

    @BeanProperty
    var player: String,

    @BeanProperty
    var cards: ByteString,

    @BeanProperty
    var hand: wire.Hand) extends Message {
  
  def schema = DeclareHandSchema.SCHEMA
  //def pipeSchema = DeclareHandSchema.PIPE_SCHEMA
  def this() = this(null, null, null, null)
}

@MsgPack
sealed case class DeclareWinner(
    @BeanProperty
    var pos: Integer,

    @BeanProperty
    var player: String,
    
    @BeanProperty
    var amount: java.lang.Double) extends Message {
  
  def schema = DeclareWinnerSchema.SCHEMA
  //def pipeSchema = DeclareWinnerSchema.PIPE_SCHEMA
  def this() = this(null, null, null)
  
}

@MsgPack
sealed case class TickTimer(
    @BeanProperty
    var pos: Integer,
    
    @BeanProperty
    var player: String,
    
    @BeanProperty
    var timeLeft: Integer,
    
    @BeanProperty
    var timeBank: java.lang.Boolean = false
) extends Message {
  
  def this() = this(null, null, null)
  def schema = TickTimerSchema.SCHEMA
  def isTimeBank = timeBank
  
}

@MsgPack
sealed case class PlayerJoin(
    @BeanProperty
    var pos: Integer,
    
    @BeanProperty
    var player: String,
    
    @BeanProperty
    var amount: java.lang.Double = null
) extends Outbound {
  
  def schema = PlayerJoinSchema.SCHEMA
  def this() = this(null, null)
  
}

@MsgPack
sealed case class Start(
    @BeanProperty
    var table: wire.Table,

    @BeanProperty
    var variation: wire.Variation,

    @BeanProperty
    var stake: wire.Stake
) extends Outbound {
  
  def this() = this(null, null, null)
  
  def schema = StartSchema.SCHEMA
}