package de.pokerno.protocol.msg

import de.pokerno.protocol.{ wire, Message ⇒ BaseMessage }

import com.dyuproject.protostuff
import beans._
import org.msgpack.annotation.{ Message ⇒ MsgPack }
import com.fasterxml.jackson.annotation.{ JsonTypeInfo, JsonSubTypes }

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "$type"
)
abstract class Message extends BaseMessage

/**
 * Msg
 */
sealed case class Chat(_body: String) extends Msg {
  body = _body
  `type` = MsgSchema.MsgType.CHAT

  def this() = this(null)

}

sealed case class Dealer(_body: String) extends Msg {
  body = _body
  `type` = MsgSchema.MsgType.DEALER

  def this() = this(null)

}

sealed case class Error(_body: String) extends Msg {
  body = _body
  `type` = MsgSchema.MsgType.ERROR

  def this() = this(null)

}
