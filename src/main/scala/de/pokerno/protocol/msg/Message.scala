package de.pokerno.protocol.msg

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.{poker, model, gameplay}
import de.pokerno.protocol.{wire, Message => BaseMessage, HasPlayer, HasCards, HasAmount}
import wire.Conversions._
import Conversions._

import com.dyuproject.protostuff
import beans._
import org.msgpack.annotation.{ Message => MsgPack }
import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonSubTypes}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "$type"
)
abstract class Message extends BaseMessage


/**
 * Command
 * */
@MsgPack
sealed case class JoinTable(
    @BeanProperty
    var pos: Integer,
    var player: model.Player,
    var amount: Decimal) extends Message with HasAmount with HasPlayer {
  
  def schema = JoinTableSchema.SCHEMA
  //def pipeSchema = JoinTableSchema.PIPE_SCHEMA
  
  def this() = this(null, null, null)
}

/**
 * Msg
 * */
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
