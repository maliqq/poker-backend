package de.pokerno.protocol.rpc

import beans._
import org.msgpack.annotation.{ Message => MsgPack }

@MsgPack
class RoomAction extends Request {
  
  def schema = RoomActionSchema.SCHEMA
  
  @BeanProperty
  var `type`: RoomActionSchema.ActionType = null
  
  @BeanProperty
  var id: String = null
  
  @BeanProperty
  var deadline: Integer = null
  
}

@MsgPack
class TableAction extends Request {
  
  def schema = TableActionSchema.SCHEMA
  
  @BeanProperty
  var `type`: TableActionSchema.ActionType = null
  
  @BeanProperty
  var joinPlayer: JoinPlayer = null
  
  @BeanProperty
  var kickPlayer: KickPlayer = null
  
  @BeanProperty
  var chat: Chat = null
  
}

@MsgPack
class DealAction extends Request {
  
  def schema = DealActionSchema.SCHEMA
  
  @BeanProperty
  var `type`: DealActionSchema.ActionType = null
  
  @BeanProperty
  var addBet: AddBet = null
  
  @BeanProperty
  var dealCards: DealCards = null
  
  @BeanProperty
  var discardCards: DiscardCards = null
  
  @BeanProperty
  var showCards: ShowCards = null
  
}

@MsgPack
class NodeAction extends Request {
  
  def schema = NodeActionSchema.SCHEMA
  
  @BeanProperty
  var `type`: NodeActionSchema.ActionType = null
  
  @BeanProperty
  var createRoom: CreateRoom = null
  
  @BeanProperty
  var addBet: AddBet = null
}
