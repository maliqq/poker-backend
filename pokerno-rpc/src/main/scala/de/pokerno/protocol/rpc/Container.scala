package de.pokerno.protocol.rpc

import beans._
import org.msgpack.annotation.{ Message ⇒ MsgPack }
import de.pokerno.protocol.{ wire, Message ⇒ BaseMessage, cmd }

@MsgPack
class RoomAction extends BaseMessage {

  def schema = RoomActionSchema.SCHEMA

  @BeanProperty
  var `type`: RoomActionSchema.ActionType = null

  @BeanProperty
  var id: String = null

  @BeanProperty
  var deadline: Integer = null

}

@MsgPack
class TableAction extends BaseMessage {

  def schema = TableActionSchema.SCHEMA

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
class DealAction extends BaseMessage {

  def schema = DealActionSchema.SCHEMA

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
class NodeAction extends BaseMessage {

  def schema = NodeActionSchema.SCHEMA

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
    
    @BeanProperty var dealAction: DealAction = null
) extends BaseMessage {
  
  def schema = RequestSchema.SCHEMA
  
  def this() = this(null)
}
