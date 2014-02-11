package de.pokerno.protocol.msg

import de.pokerno.protocol.{ wire, Message ⇒ BaseMessage }

import com.dyuproject.protostuff
import com.dyuproject.protostuff.ByteString
import beans._
import org.msgpack.annotation.{ Message ⇒ MsgPack }
import com.fasterxml.jackson.annotation.{ JsonTypeInfo, JsonSubTypes, JsonIgnoreProperties }

@JsonSubTypes(Array(
  new JsonSubTypes.Type(value = classOf[JoinTable], name = "table:join"),
  new JsonSubTypes.Type(value = classOf[AddBet], name = "bet:add"),
  new JsonSubTypes.Type(value = classOf[DiscardCards], name = "cards:discard"),
  new JsonSubTypes.Type(value = classOf[ShowCards], name = "cards:show")
))
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class Inbound extends Message

/**
 * Command
 */
@MsgPack
sealed case class JoinTable(
    @BeanProperty var pos: Integer,

    @BeanProperty var amount: java.lang.Double) extends Inbound {

  def schema = JoinTableSchema.SCHEMA
  //def pipeSchema = JoinTableSchema.PIPE_SCHEMA

  def this() = this(null, null)
}
/**
 * Action event
 */
@MsgPack
@JsonIgnoreProperties(ignoreUnknown = true)
sealed case class AddBet(
    @BeanProperty var bet: wire.Bet) extends Inbound {
  def schema = AddBetSchema.SCHEMA

  def this() = this(null)
}

@MsgPack
@JsonIgnoreProperties(ignoreUnknown = true)
sealed case class DiscardCards(
    @BeanProperty var cards: ByteString) extends Inbound {
  def schema = DiscardCardsSchema.SCHEMA

  @BeanProperty
  var cardsNum: Integer = null

  @BeanProperty
  var `type`: DiscardCardsSchema.DiscardType = null

  def this() = this(null)

}

@MsgPack
@JsonIgnoreProperties(ignoreUnknown = true)
sealed case class ShowCards(
    @BeanProperty var cards: ByteString,
    @BeanProperty var muck: java.lang.Boolean = null) extends Inbound {

  def schema = ShowCardsSchema.SCHEMA

  @BeanProperty
  var `type`: ShowCardsSchema.ShowType = if (muck)
    ShowCardsSchema.ShowType.MUCK
  else
    ShowCardsSchema.ShowType.SHOW

  def this() = this(null)

}
