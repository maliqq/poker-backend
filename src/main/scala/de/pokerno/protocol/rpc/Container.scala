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
  var kickPlayer: KickPlayer = null
}

@MsgPack
class NodeAction extends Request {
  def schema = NodeActionSchema.SCHEMA
  @BeanProperty
  var `type`: NodeActionSchema.ActionType = null
  @BeanProperty
  var createRoom: CreateRoom = null
}
