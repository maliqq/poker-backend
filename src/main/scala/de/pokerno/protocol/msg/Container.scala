package de.pokerno.protocol.msg

import de.pokerno.{model, poker}
import de.pokerno.protocol.HasPlayer
import de.pokerno.protocol.wire

import reflect._
import math.{BigDecimal => Decimal}

import com.fasterxml.jackson.annotation.JsonInclude
import com.dyuproject.protostuff
import com.dyuproject.protostuff.ByteString
import org.msgpack.annotation.{ Message => MsgPack }

@MsgPack
@JsonInclude()
class ActionEvent(
  @BeanProperty
  var `type`: ActionEventSchema.EventType,
  
  @BeanProperty
  var pos: Integer = null,
  
  var player: model.Player = null,
  
  @BeanProperty
  var cardsDiscard: CardsDiscard = null,
  
  @BeanProperty
  var cardsShow: CardsShow = null,
  
  @BeanProperty
  var betAdd: BetAdd = null
) extends Outbound with HasPlayer {
  
  def this() = this(null)
  
  def schema = ActionEventSchema.SCHEMA
  //def pipeSchema = ActionEventSchema.PIPE_SCHEMA
}

@MsgPack
class GameplayEvent extends Message {
  def schema = GameplayEventSchema.SCHEMA
  //def pipeSchema = GameplayEventSchema.PIPE_SCHEMA
  @BeanProperty
  var `type`: GameplayEventSchema.EventType = null
  @BeanProperty
  var game: wire.Game = null
  @BeanProperty
  var stake: wire.Stake = null
}

@MsgPack
class StageEvent extends Message {
  def schema = StageEventSchema.SCHEMA
  //def pipeSchema = StageEventSchema.PIPE_SCHEMA
  @BeanProperty
  var `type`: StageEventSchema.EventType = null
  @BeanProperty
  var stage: StageEventSchema.StageType = null
  @BeanProperty
  var street: StageEventSchema.StreetType = null
}

@MsgPack
class TableEvent extends Outbound {
  def schema = TableEventSchema.SCHEMA
  //def pipeSchema = TableEventSchema.PIPE_SCHEMA
  @BeanProperty
  var `type`: TableEventSchema.EventType = null
  @BeanProperty
  var button: Integer = null
  @BeanProperty
  var state: TableEventSchema.TableState = null
}

@MsgPack
class SeatEvent(_type: SeatEventSchema.EventType) extends Outbound {
  def schema = SeatEventSchema.SCHEMA
  //def pipeSchema = SeatEventSchema.PIPE_SCHEMA
  @BeanProperty
  var `type` = _type
  @BeanProperty
  var pos: Integer = null
  @BeanProperty
  var seat: wire.Seat = null
  def this() = this(null)
}

@MsgPack
class DealEvent extends Outbound {
  def schema = DealEventSchema.SCHEMA
  //def pipeSchema = DealEventSchema.PIPE_SCHEMA
  
  @BeanProperty
  var `type`: DealEventSchema.EventType = null

  @BeanProperty
  var dealCards: DealCards = null
  
  @BeanProperty
  var requireBet: RequireBet = null
  
  @BeanProperty
  var requireDiscard: RequireDiscard = null
  
  @BeanProperty
  var declarePot: DeclarePot = null

  @BeanProperty
  var declareHand: DeclareHand = null
  
  @BeanProperty
  var declareWinner: DeclareWinner = null
  
  @BeanProperty
  var tickTimer: TickTimer = null
}

@MsgPack
class Msg extends Message {
  def schema = MsgSchema.SCHEMA
  //def pipeSchema = MsgSchema.PIPE_SCHEMA
  @BeanProperty
  var `type`: MsgSchema.MsgType = null
  @BeanProperty
  var body: String = null
}

@MsgPack
class Cmd extends Message {
  def schema = CmdSchema.SCHEMA
  //def pipeSchema = CmdSchema.PIPE_SCHEMA
  @BeanProperty
  var `type`: CmdSchema.CmdType = null
  @BeanProperty
  var joinTable: JoinTable = null
  @BeanProperty
  var actionEvent: ActionEvent = null
}

@MsgPack
class Event extends Message {
  def schema = EventSchema.SCHEMA
  //def pipeSchema = EventSchema.PIPE_SCHEMA
  @BeanProperty
  var `type`: EventSchema.EventType = null
  @BeanProperty
  var seatEvent: SeatEvent = null
  @BeanProperty
  var actionEvent: ActionEvent = null
  @BeanProperty
  var stageEvent: StageEvent = null
  @BeanProperty
  var tableEvent: TableEvent = null
  @BeanProperty
  var dealEvent: DealEvent = null
  @BeanProperty
  var gameplayEvent: GameplayEvent = null
}
