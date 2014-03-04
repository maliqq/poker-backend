package de.pokerno.protocol.msg

import de.pokerno.protocol.{ wire, Message ⇒ BaseMessage }

import com.dyuproject.protostuff
import com.dyuproject.protostuff.ByteString
import beans._
import org.msgpack.annotation.{ Message ⇒ MsgPack }
import com.fasterxml.jackson.annotation.{ JsonInclude, JsonTypeInfo, JsonSubTypes }
import proto.msg._

@JsonSubTypes(Array(
  new JsonSubTypes.Type(value = classOf[BetAdd], name = "bet:add"),
  new JsonSubTypes.Type(value = classOf[CardsDiscard], name = "cards:discard"),
  new JsonSubTypes.Type(value = classOf[CardsShow], name = "cards:show"),

  new JsonSubTypes.Type(value = classOf[DealCards], name = "cards:deal"),
  new JsonSubTypes.Type(value = classOf[RequireBet], name = "bet:ask"),
  new JsonSubTypes.Type(value = classOf[RequireDiscard], name = "discard:ask"),
  new JsonSubTypes.Type(value = classOf[DeclarePot], name = "pot:"),
  new JsonSubTypes.Type(value = classOf[DeclareHand], name = "hand:"),
  new JsonSubTypes.Type(value = classOf[DeclareWinner], name = "winner:"),
  new JsonSubTypes.Type(value = classOf[TickTimer], name = "timer:tick"),

  //  new JsonSubTypes.Type(value = classOf[StageEvent], name="StageEvent"),
  //  new JsonSubTypes.Type(value = classOf[GameplayEvent], name="GameplayEvent"),
  //  new JsonSubTypes.Type(value = classOf[DealEvent], name="DealEvent"),
  new JsonSubTypes.Type(value = classOf[SeatEvent], name="seat:"),

  new JsonSubTypes.Type(value = classOf[Start], name = "start:"),
  new JsonSubTypes.Type(value = classOf[ButtonChange], name = "table:"),
  new JsonSubTypes.Type(value = classOf[GameChange], name = "gameplay:"),
  new JsonSubTypes.Type(value = classOf[StakeChange], name = "gameplay:"),
  new JsonSubTypes.Type(value = classOf[PlayStart], name = "stage:"),
  new JsonSubTypes.Type(value = classOf[PlayStop], name = "stage:"),
  new JsonSubTypes.Type(value = classOf[StreetStart], name = "stage:"),
  new JsonSubTypes.Type(value = classOf[PlayerJoin], name = "player:join"),
  new JsonSubTypes.Type(value = classOf[PlayerLeave], name = "player:leave"),
  new JsonSubTypes.Type(value = classOf[Chat], name = "msg:"),
  new JsonSubTypes.Type(value = classOf[Dealer], name = "msg:"),
  new JsonSubTypes.Type(value = classOf[Error], name = "msg:")
))
abstract class Outbound extends Message

@MsgPack
sealed case class BetAdd(
    @BeanProperty
    var pos: Integer,

    @BeanProperty
    var player: String,

    @BeanProperty
    var bet: wire.Bet) extends Outbound {
  def schema = BetAddSchema.getSchema()
  def this() = this(null, null, null)
}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class CardsDiscard(
    @BeanProperty
    var pos: Integer,

    @BeanProperty
    var player: String,

    @BeanProperty
    var cards: ByteString,

    @BeanProperty
    var cardsNum: Integer = null) extends Outbound {
  def schema = CardsDiscardSchema.getSchema()
  def this() = this(null, null, null)
}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class CardsShow(
    @BeanProperty
    var pos: Integer,

    @BeanProperty
    var player: String,

    @BeanProperty
    var cards: ByteString,

    @BooleanBeanProperty
    var muck: java.lang.Boolean = null) extends Outbound {
  def schema = CardsShowSchema.getSchema()
  def this() = this(null, null, null)
}
/**
 * Table event
 */
@MsgPack
sealed case class ButtonChange(
    _button: Integer) extends TableEvent(TableEventSchema.EventType.BUTTON) {

  setButton(_button)

  def this() = this(null)

}

/**
 * Gameplay event
 */
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
 */
@MsgPack
sealed case class PlayStart(_play: Play) extends StageEvent(StageEventSchema.EventType.START) {

  stage = StageEventSchema.StageType.PLAY
  
  play = _play

}

@MsgPack
sealed case class PlayStop() extends StageEvent(StageEventSchema.EventType.STOP) {

  stage = StageEventSchema.StageType.PLAY
  //def this() = this()

}

import proto.wire.StreetType
@MsgPack
sealed case class StreetStart(streetName: StreetType) extends StageEvent(StageEventSchema.EventType.START) {

  stage = StageEventSchema.StageType.STREET
  street = streetName
  def this() = this(null)

}

/**
 * Deal event
 */

import proto.wire.DealType
@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DealCards(

    @BeanProperty
    var `type`: DealType,

    @BeanProperty
    var cards: ByteString = null,

    @BeanProperty
    var pos: Integer = null,

    @BeanProperty
    var player: String = null,

    @BeanProperty
    var cardsNum: Integer = null) extends Outbound {

  def this() = this(null)

  def schema = DealCardsSchema.getSchema()
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
    var raise: wire.Range) extends Outbound {

  def schema = RequireBetSchema.getSchema()
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
    var player: String) extends Outbound {

  def schema = RequireDiscardSchema.getSchema()
  //def pipeSchema = RequireDiscardSchema.PIPE_SCHEMA
  def this() = this(null, null)

}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclarePot(
    @BeanProperty
    var pot: java.lang.Double,

    @BeanProperty
    var side: java.util.ArrayList[java.lang.Double] = null,

    @BeanProperty
    var rake: java.lang.Double = null) extends Outbound {

  def schema = DeclarePotSchema.getSchema()
  //def pipeSchema = DeclarePotSchema.PIPE_SCHEMA

  def this() = this(null)
}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclareHand(
    @BeanProperty
    var pos: Integer,

    @BeanProperty
    var player: String,

    @BeanProperty
    var cards: ByteString,

    @BeanProperty
    var hand: wire.Hand) extends Outbound {

  def schema = DeclareHandSchema.getSchema()
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
    var amount: java.lang.Double) extends Outbound {

  def schema = DeclareWinnerSchema.getSchema()
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

    @BooleanBeanProperty
    var timeBank: java.lang.Boolean = false) extends Outbound {

  def this() = this(null, null, null)
  def schema = TickTimerSchema.getSchema()

}

@MsgPack
sealed case class PlayerJoin(
    @BeanProperty
    var pos: Integer,

    @BeanProperty
    var player: String,

    @BeanProperty
    var amount: java.lang.Double = null) extends Outbound {

  def schema = PlayerJoinSchema.getSchema()
  def this() = this(null, null)

}

@MsgPack
sealed case class PlayerLeave(
    @BeanProperty
    var pos: Integer,

    @BeanProperty
    var player: String) extends Outbound {

  def schema = PlayerLeaveSchema.getSchema()
  def this() = this(null, null)
}

@MsgPack
sealed case class Start(
    @BeanProperty
    var table: wire.Table,

    @BeanProperty
    var variation: wire.Variation,

    @BeanProperty
    var stake: wire.Stake,
    
    @BeanProperty
    var play: Play
    ) extends Outbound {

  def this() = this(null, null, null, null)

  def schema = StartSchema.getSchema()
}

import proto.wire.StreetType
@MsgPack
sealed case class Play(
    @BeanProperty
    var id: String,
    
    @BeanProperty
    var startAt: java.lang.Long,
    
    @BeanProperty
    var stopAt: java.lang.Long = null,
    
    @BeanProperty
    var street: StreetType = null,
    
    @BeanProperty
    var acting: RequireBet = null,
    
    @BeanProperty
    var pot: java.lang.Double = null,
    
    @BeanProperty
    var rake: java.lang.Double = null,
    
    @BeanProperty
    var board: ByteString = null,
    
    @BeanProperty
    var winners: java.util.ArrayList[DeclareWinner] = null,
    
    @BeanProperty
    var knownCards: java.util.ArrayList[CardsShow] = null
) {
  def this() = this(null, null)
  def schema = PlaySchema.getSchema()
}
