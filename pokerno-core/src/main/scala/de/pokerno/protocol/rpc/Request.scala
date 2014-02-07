package de.pokerno.protocol.rpc

import de.pokerno.protocol.{ wire, Message ⇒ BaseMessage }

import beans._
import com.dyuproject.protostuff.ByteString
import org.msgpack.annotation.{ Message ⇒ MsgPack }
import com.fasterxml.jackson.annotation.{ JsonTypeInfo, JsonSubTypes }

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "$method"
)
@JsonSubTypes(Array(
  // create new room
  new JsonSubTypes.Type(value = classOf[CreateRoom], name = "CreateRoom")
))
abstract class Request extends BaseMessage {
}

/**
 * Node event
 */
@MsgPack
sealed case class CreateRoom(
    @BeanProperty var id: String,
    @BeanProperty var table: wire.Table,
    @BeanProperty var variation: wire.Variation,
    @BeanProperty var stake: wire.Stake) extends Request {

  def schema = CreateRoomSchema.SCHEMA

  def this() = this(null, null, null, null)

}

/**
 * Table action
 */

@MsgPack
sealed case class JoinPlayer(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: String,

    @BeanProperty var amount: java.lang.Double) extends Request {

  def schema = JoinPlayerSchema.SCHEMA
  //def pipeSchema = JoinTableSchema.PIPE_SCHEMA

  def this() = this(null, null, null)
}

@MsgPack
sealed case class KickPlayer(
    @BeanProperty var player: String,

    @BeanProperty var reason: String) extends Request {

  def schema = KickPlayerSchema.SCHEMA

  def this() = this(null, null)

}

@MsgPack
sealed case class Chat(
    @BeanProperty var player: String,

    @BeanProperty var body: String) extends Request {

  def schema = ChatSchema.SCHEMA

  def this() = this(null, null)

}

@MsgPack
sealed case class AddBet(

    @BeanProperty var player: String,

    @BeanProperty var bet: wire.Bet) extends Request {

  def schema = AddBetSchema.SCHEMA
  def this() = this(null, null)
}

@MsgPack
sealed case class DealCards(
    @BeanProperty var `type`: wire.DealType,

    @BeanProperty var player: String = null,

    @BeanProperty var cards: ByteString = null,

    @BeanProperty var cardsNum: Integer = null) extends Request {

  def schema = DealCardsSchema.SCHEMA
  def this() = this(null)
}

@MsgPack
sealed case class DiscardCards(
    @BeanProperty var cards: ByteString,

    @BeanProperty var player: String) extends Request {

  def schema = DealCardsSchema.SCHEMA

  def this() = this(null, null)

}

@MsgPack
sealed case class ShowCards(
    @BeanProperty var cards: ByteString,

    @BeanProperty var player: String,

    @BeanProperty var muck: java.lang.Boolean = null) extends Request {

  def schema = ShowCardsSchema.SCHEMA

  def this() = this(null, null)

  def isMuck = getMuck
}
