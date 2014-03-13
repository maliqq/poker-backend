package de.pokerno.protocol.rpc

import beans._
import org.msgpack.annotation.{ Message ⇒ MsgPack }
import com.fasterxml.jackson.annotation.{ JsonTypeInfo, JsonSubTypes, JsonIgnoreProperties }
import de.pokerno.protocol.{ wire, Message ⇒ BaseMessage, cmd }
import proto.rpc._

import com.fasterxml.jackson.annotation.{ JsonTypeInfo, JsonSubTypes, JsonIgnoreProperties }

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "$type"
)
@JsonSubTypes(Array(
  new JsonSubTypes.Type(value = classOf[NodeAction], name = "$node"),
  new JsonSubTypes.Type(value = classOf[RoomAction], name = "$room"),
  new JsonSubTypes.Type(value = classOf[TableAction], name = "$table"),
  new JsonSubTypes.Type(value = classOf[DealAction], name = "$deal")
))
abstract class BaseRequest extends BaseMessage

@MsgPack
class RoomAction extends BaseRequest {

  def schema = RoomActionSchema.getSchema()

  @BeanProperty
  var `type`: RoomActionSchema.ActionType = null

  @BeanProperty
  var id: String = null

  @BeanProperty
  var deadline: Integer = null

}

@MsgPack
class TableAction extends BaseRequest {

  def schema = TableActionSchema.getSchema()

  @BeanProperty
  var `type`: TableActionSchema.ActionType = null

  @BeanProperty
  var joinPlayer: cmd.JoinPlayer = null

  @BeanProperty
  var kickPlayer: cmd.KickPlayer = null

  @BeanProperty
  var chat: cmd.Chat = null

}

@MsgPack
class DealAction extends BaseRequest {

  def schema = DealActionSchema.getSchema()

  @BeanProperty
  var `type`: DealActionSchema.ActionType = null

  @BeanProperty
  var addBet: cmd.AddBet = null

  @BeanProperty
  var dealCards: cmd.DealCards = null

  @BeanProperty
  var discardCards: cmd.DiscardCards = null

  @BeanProperty
  var showCards: cmd.ShowCards = null

}

@MsgPack
class NodeAction extends BaseRequest {

  def schema = NodeActionSchema.getSchema()

  @BeanProperty
  var `type`: NodeActionSchema.ActionType = null

  @BeanProperty
  var createRoom: CreateRoom = null

  @BeanProperty
  var addBet: cmd.AddBet = null
}

@MsgPack
sealed case class Request(
    @BeanProperty var `type`: RequestSchema.RequestType,

    @BeanProperty var nodeAction: NodeAction = null,

    @BeanProperty var roomAction: RoomAction = null,

    @BeanProperty var tableAction: TableAction = null,

    @BeanProperty var dealAction: DealAction = null) extends BaseMessage {

  def schema = RequestSchema.getSchema()

  def this() = this(null)
}
