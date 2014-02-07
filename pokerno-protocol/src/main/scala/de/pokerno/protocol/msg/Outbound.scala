package de.pokerno.protocol.msg

import de.pokerno.protocol.{ wire, Message ⇒ BaseMessage }

import com.dyuproject.protostuff
import com.dyuproject.protostuff.ByteString
import beans._
import org.msgpack.annotation.{ Message ⇒ MsgPack }
import com.fasterxml.jackson.annotation.{ JsonInclude, JsonTypeInfo, JsonSubTypes }

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

  new JsonSubTypes.Type(value = classOf[Start], name = "start:"),
  new JsonSubTypes.Type(value = classOf[ButtonChange], name = "table:"),
  new JsonSubTypes.Type(value = classOf[GameChange], name = "gameplay:"),
  new JsonSubTypes.Type(value = classOf[StakeChange], name = "gameplay:"),
  new JsonSubTypes.Type(value = classOf[PlayStart], name = "stage:"),
  new JsonSubTypes.Type(value = classOf[PlayStop], name = "stage:"),
  new JsonSubTypes.Type(value = classOf[StreetStart], name = "stage:"),
  new JsonSubTypes.Type(value = classOf[PlayerJoin], name = "player:join"),
  new JsonSubTypes.Type(value = classOf[Chat], name = "msg:"),
  new JsonSubTypes.Type(value = classOf[Dealer], name = "msg:"),
  new JsonSubTypes.Type(value = classOf[Error], name = "msg:")
))
abstract class Outbound extends Message

@MsgPack
sealed case class BetAdd(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: String,

    @BeanProperty var bet: wire.Bet) extends Outbound {
  def schema = BetAddSchema.SCHEMA
  def this() = this(null, null, null)
}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class CardsDiscard(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: String,

    @BeanProperty var cards: ByteString,

    @BeanProperty var cardsNum: Integer = null) extends Outbound {
  def schema = CardsDiscardSchema.SCHEMA
  def this() = this(null, null, null)
}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class CardsShow(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: String,

    @BeanProperty var cards: ByteString,

    @BeanProperty var muck: java.lang.Boolean = null) extends Outbound {
  def schema = CardsShowSchema.SCHEMA
  def this() = this(null, null, null)
  def isMuck = getMuck
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
 */

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DealCards(

    @BeanProperty var `type`: wire.DealType,

    @BeanProperty var cards: ByteString = null,

    @BeanProperty var pos: Integer = null,

    @BeanProperty var player: String = null,

    @BeanProperty var cardsNum: Integer = null) extends Outbound {

  def this() = this(null)

  def schema = DealCardsSchema.SCHEMA
  //def pipeSchema = DealCardsSchema.PIPE_SCHEMA

}

@MsgPack
sealed case class RequireBet(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: String,

    @BeanProperty var call: java.lang.Double,

    @BeanProperty var raise: wire.Range) extends Outbound {

  def schema = RequireBetSchema.SCHEMA
  //def pipeSchema = RequireBetSchema.PIPE_SCHEMA

  def this() = this(null, null, null, wire.Range(.0, .0))
  //@BeanProperty
  //var `type` = DealEventSchema.EventType.REQUIRE_BET

}

@MsgPack
sealed case class RequireDiscard(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: String) extends Outbound {

  def schema = RequireDiscardSchema.SCHEMA
  //def pipeSchema = RequireDiscardSchema.PIPE_SCHEMA
  def this() = this(null, null)

}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclarePot(
    @BeanProperty var pot: java.lang.Double,

    @BeanProperty var side: java.util.ArrayList[java.lang.Double] = null,

    @BeanProperty var rake: java.lang.Double = null) extends Outbound {

  def schema = DeclarePotSchema.SCHEMA
  //def pipeSchema = DeclarePotSchema.PIPE_SCHEMA

  def this() = this(null)
}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclareHand(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: String,

    @BeanProperty var cards: ByteString,

    @BeanProperty var hand: wire.Hand) extends Outbound {

  def schema = DeclareHandSchema.SCHEMA
  //def pipeSchema = DeclareHandSchema.PIPE_SCHEMA
  def this() = this(null, null, null, null)
}

@MsgPack
sealed case class DeclareWinner(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: String,

    @BeanProperty var amount: java.lang.Double) extends Outbound {

  def schema = DeclareWinnerSchema.SCHEMA
  //def pipeSchema = DeclareWinnerSchema.PIPE_SCHEMA
  def this() = this(null, null, null)

}

@MsgPack
sealed case class TickTimer(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: String,

    @BeanProperty var timeLeft: Integer,

    @BeanProperty var timeBank: java.lang.Boolean = false) extends Outbound {

  def this() = this(null, null, null)
  def schema = TickTimerSchema.SCHEMA
  def isTimeBank = timeBank

}

@MsgPack
sealed case class PlayerJoin(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: String,

    @BeanProperty var amount: java.lang.Double = null) extends Outbound {

  def schema = PlayerJoinSchema.SCHEMA
  def this() = this(null, null)

}

@MsgPack
sealed case class Start(
    @BeanProperty var table: wire.Table,

    @BeanProperty var variation: wire.Variation,

    @BeanProperty var stake: wire.Stake) extends Outbound {

  def this() = this(null, null, null)

  def schema = StartSchema.SCHEMA
}