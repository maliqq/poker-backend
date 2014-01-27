package de.pokerno.protocol.msg

import de.pokerno.protocol.wire

import reflect._

import com.fasterxml.jackson.annotation.JsonInclude
import com.dyuproject.protostuff
import com.dyuproject.protostuff.ByteString
import org.msgpack.annotation.{ Message => MsgPack }

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
class ActionEvent(
  @BeanProperty
  var `type`: ActionEventSchema.EventType
) extends Outbound {

  @BeanProperty
  var pos: Integer = null
  
  @BeanProperty
  var player: String = null
  
  @BeanProperty
  var cardsDiscard: CardsDiscard = null
  
  @BeanProperty
  var cardsShow: CardsShow = null
  
  @BeanProperty
  var betAdd: BetAdd = null
  
  def this() = this(null)
  
  def schema = ActionEventSchema.SCHEMA
  //def pipeSchema = ActionEventSchema.PIPE_SCHEMA
}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
class GameplayEvent(
    @BeanProperty
    var `type`: GameplayEventSchema.EventType
  ) extends Outbound {

  def schema = GameplayEventSchema.SCHEMA
  //def pipeSchema = GameplayEventSchema.PIPE_SCHEMA
  
  @BeanProperty
  var game: wire.Game = null

  @BeanProperty
  var stake: wire.Stake = null

  def this() = this(null)
}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
class StageEvent(
    @BeanProperty
    var `type`: StageEventSchema.EventType
  ) extends Outbound {
  def schema = StageEventSchema.SCHEMA
  //def pipeSchema = StageEventSchema.PIPE_SCHEMA
  
  @BeanProperty
  var stage: StageEventSchema.StageType = null
  
  @BeanProperty
  var street: StageEventSchema.StreetType = null

  def this() = this(null)
}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
class TableEvent(
    @BeanProperty
    var `type`: TableEventSchema.EventType
  ) extends Outbound {
  
  def schema = TableEventSchema.SCHEMA
  //def pipeSchema = TableEventSchema.PIPE_SCHEMA
  
  @BeanProperty
  var button: Integer = null

  @BeanProperty
  var state: TableEventSchema.TableState = null

  def this() = this(null)
}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
class SeatEvent(
    @BeanProperty
    var `type`: SeatEventSchema.EventType
  ) extends Outbound {
  def schema = SeatEventSchema.SCHEMA
  //def pipeSchema = SeatEventSchema.PIPE_SCHEMA
  
  @BeanProperty
  var pos: Integer = null
  
  @BeanProperty
  var seat: wire.Seat = null
  
  def this() = this(null)
}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
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
@JsonInclude(JsonInclude.Include.NON_NULL)
class Msg extends Outbound {
  def schema = MsgSchema.SCHEMA
  //def pipeSchema = MsgSchema.PIPE_SCHEMA
  @BeanProperty
  var `type`: MsgSchema.MsgType = null
  @BeanProperty
  var body: String = null
}

@MsgPack
@JsonInclude(JsonInclude.Include.NON_NULL)
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
@JsonInclude(JsonInclude.Include.NON_NULL)
class Evt extends Outbound {
  def schema = EvtSchema.SCHEMA
  //def pipeSchema = EventSchema.PIPE_SCHEMA
  @BeanProperty
  var `type`: EvtSchema.EventType = null
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
