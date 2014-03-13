package de.pokerno.protocol.rpc

import beans._
import com.dyuproject.protostuff.ByteString
import org.msgpack.annotation.{ Message ⇒ MsgPack }
import com.fasterxml.jackson.annotation.{ JsonTypeInfo, JsonSubTypes }
import de.pokerno.protocol.{ wire, Message ⇒ BaseMessage }
import proto.rpc._

//@JsonTypeInfo(
//  use = JsonTypeInfo.Id.NAME,
//  include = JsonTypeInfo.As.PROPERTY,
//  property = "$method"
//)
//@JsonSubTypes(Array(
//  // create new room
//  new JsonSubTypes.Type(value = classOf[CreateRoom], name = "CreateRoom")
//))
//abstract class Request extends BaseMessage {
//}

/**
 * Node event
 */
@MsgPack
sealed case class CreateRoom(
    @BeanProperty var id: String,

    @BeanProperty var table: wire.Table,

    @BeanProperty var variation: wire.Variation,

    @BeanProperty var stake: wire.Stake) extends BaseMessage {

  def schema = CreateRoomSchema.getSchema()

  def this() = this(null, null, null, null)

}

/**
 * Table action
 */
