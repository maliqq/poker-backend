package de.pokerno.backend.protocol

import de.pokerno.model
import de.pokerno.poker

import reflect._
import math.{BigDecimal => Decimal}

import com.dyuproject.protostuff
import com.dyuproject.protostuff.ByteString
import org.msgpack.annotation.{ Message => MsgPack }

@MsgPack
class ActionEvent extends Message {
  def schema = ActionEventSchema.SCHEMA
  //def pipeSchema = ActionEventSchema.PIPE_SCHEMA
  @BeanProperty var `type`: ActionEventSchema.EventType = null
  @BeanProperty var discardCards: DiscardCards = null
  @BeanProperty var showCards: ShowCards = null
  @BeanProperty var addBet: AddBet = null
}

@MsgPack
class GameplayEvent extends Message {
  def schema = GameplayEventSchema.SCHEMA
  //def pipeSchema = GameplayEventSchema.PIPE_SCHEMA
  @BeanProperty var `type`: GameplayEventSchema.EventType = null
  @BeanProperty var game: Game = null
  @BeanProperty var stake: Stake = null
}

@MsgPack
class StageEvent extends Message {
  def schema = StageEventSchema.SCHEMA
  //def pipeSchema = StageEventSchema.PIPE_SCHEMA
  @BeanProperty var `type`: StageEventSchema.EventType = null
  @BeanProperty var stage: StageEventSchema.StageType = null
  @BeanProperty var street: StageEventSchema.StreetType = null
}

@MsgPack
class TableEvent extends Message {
  def schema = TableEventSchema.SCHEMA
  //def pipeSchema = TableEventSchema.PIPE_SCHEMA
  @BeanProperty var `type`: TableEventSchema.EventType = null
  @BeanProperty var button: Integer = null
  @BeanProperty var state: TableEventSchema.TableState = null
}

@MsgPack
class SeatEvent(_type: SeatEventSchema.EventType) extends Message {
  def schema = SeatEventSchema.SCHEMA
  //def pipeSchema = SeatEventSchema.PIPE_SCHEMA
  @BeanProperty var `type` = _type
  @BeanProperty var pos: Integer = null
  @BeanProperty var seat: Seat = null
  def this() = this(null)
}

@MsgPack
class DealEvent extends Message {
  def schema = DealEventSchema.SCHEMA
  //def pipeSchema = DealEventSchema.PIPE_SCHEMA
  @BeanProperty var `type`: DealEventSchema.EventType = null
  @BeanProperty var requireBet: RequireBet = null
  @BeanProperty var requireDiscard: RequireDiscard = null
  @BeanProperty var declarePot: DeclarePot = null
  @BeanProperty var declareWinner: DeclareWinner = null
  @BeanProperty var tickTimer: TickTimer = null
}

@MsgPack
class Msg extends Message {
  def schema = MsgSchema.SCHEMA
  //def pipeSchema = MsgSchema.PIPE_SCHEMA
  @BeanProperty var `type`: MsgSchema.MsgType = null
  @BeanProperty var body: String = null
}

@MsgPack
class Cmd extends Message {
  def schema = CmdSchema.SCHEMA
  //def pipeSchema = CmdSchema.PIPE_SCHEMA
  @BeanProperty var `type`: CmdSchema.CmdType = null
  @BeanProperty var joinTable: JoinTable = null
  @BeanProperty var actionEvent: ActionEvent = null
}

@MsgPack
class Event extends Message {
  def schema = EventSchema.SCHEMA
  //def pipeSchema = EventSchema.PIPE_SCHEMA
  @BeanProperty var `type`: EventSchema.EventType = null
  @BeanProperty var seatEvent: SeatEvent = null
  @BeanProperty var actionEvent: ActionEvent = null
  @BeanProperty var stageEvent: StageEvent = null
  @BeanProperty var tableEvent: TableEvent = null
  @BeanProperty var dealEvent: DealEvent = null
  @BeanProperty var gameplayEvent: GameplayEvent = null
}

@MsgPack
class RoomAction extends Message {
  def schema = RoomActionSchema.SCHEMA
  @BeanProperty var `type`: RoomActionSchema.ActionType = null
  @BeanProperty var id: String = null
  @BeanProperty var deadline: Integer = null
}

@MsgPack
class TableAction extends Message {
  def schema = TableActionSchema.SCHEMA
  @BeanProperty var `type`: TableActionSchema.ActionType = null
  @BeanProperty var kickPlayer: KickPlayer = null
  @BeanProperty var moveButton: MoveButton = null
}

trait HasPlayer {
  def player: model.Player
  def player_=(v: model.Player)
  
  def getPlayer: String = if (player != null) player.id else null
  def setPlayer(v: String) = player = new model.Player(v)
}

trait HasAmount {
  def amount: Decimal
  def amount_=(v: Decimal)
  
  def getAmount: java.lang.Double = if (amount != null) amount.toDouble else null
  def setAmount(v: java.lang.Double) = amount = v.toDouble
}

trait HasCards {
  implicit def byteString2Cards(v: protostuff.ByteString): List[poker.Card] = v.toByteArray.map(poker.Card.wrap(_)).toList
  implicit def cards2ByteString(v: List[poker.Card]): protostuff.ByteString = protostuff.ByteString.copyFrom(v.map(_.toByte).toArray)
  
  def cards: List[poker.Card]
  def cards_=(v: List[poker.Card])
  
  def getCards: ByteString = cards
  def setCards(v: ByteString) = cards = v 
}


import com.fasterxml.jackson.core.{JsonParser, JsonGenerator}
import com.fasterxml.jackson.databind.{SerializerProvider, DeserializationContext}
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class ByteStringSerializer extends StdSerializer[ByteString](classOf[ByteString]) {
  override def serialize(o: ByteString, g: JsonGenerator, p: SerializerProvider) {
    g.writeObject(o.toByteArray)
  }
}

class ByteStringDeserializer extends StdDeserializer[ByteString](classOf[ByteString]) {
  override def deserialize(p: JsonParser, ctx: DeserializationContext): ByteString =
    ByteString.copyFrom(p.getBinaryValue)
}
