package de.pokerno.protocol.cmd

import beans._
import com.dyuproject.protostuff.ByteString
import org.msgpack.annotation.{ Message ⇒ MsgPack }
import com.fasterxml.jackson.annotation.{ JsonTypeInfo, JsonSubTypes }
import de.pokerno.protocol.{ wire, Message ⇒ BaseMessage }

abstract class Cmd extends BaseMessage

case class PlayerEvent(
    @BeanProperty var `type`: PlayerEventSchema.EventType,
    @BeanProperty var player: String) extends Cmd {
  def schema = PlayerEventSchema.SCHEMA
  def this() = this(null, null)
}

case class StackEvent(
    @BeanProperty var `type`: StackEventSchema.EventType,
    @BeanProperty var player: String,
    @BeanProperty var amount: java.lang.Double) extends Cmd {
  def schema = StackEventSchema.SCHEMA
  def this() = this(null, null, null)
}

@MsgPack
sealed case class JoinPlayer(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: String,

    @BeanProperty var amount: java.lang.Double) extends Cmd {

  def schema = JoinPlayerSchema.SCHEMA
  //def pipeSchema = JoinTableSchema.PIPE_SCHEMA

  def this() = this(null, null, null)
}

@MsgPack
sealed case class KickPlayer(
    @BeanProperty var player: String,

    @BeanProperty var reason: String) extends Cmd {

  def schema = KickPlayerSchema.SCHEMA

  def this() = this(null, null)

}

@MsgPack
sealed case class Chat(
    @BeanProperty var player: String,

    @BeanProperty var body: String) extends Cmd {

  def schema = ChatSchema.SCHEMA

  def this() = this(null, null)

}

@MsgPack
sealed case class AddBet(

    @BeanProperty var player: String,

    @BeanProperty var bet: wire.Bet) extends Cmd {

  def schema = AddBetSchema.SCHEMA
  def this() = this(null, null)
}

@MsgPack
sealed case class DealCards(
    @BeanProperty var `type`: wire.DealType,

    @BeanProperty var player: String = null,

    @BeanProperty var cards: ByteString = null,

    @BeanProperty var cardsNum: Integer = null) extends Cmd {

  def schema = DealCardsSchema.SCHEMA
  def this() = this(null)
}

@MsgPack
sealed case class DiscardCards(
    @BeanProperty var cards: ByteString,

    @BeanProperty var player: String) extends Cmd {

  def schema = DealCardsSchema.SCHEMA

  def this() = this(null, null)

}

@MsgPack
sealed case class ShowCards(
    @BeanProperty var cards: ByteString,

    @BeanProperty var player: String,

    @BeanProperty var muck: java.lang.Boolean = null) extends Cmd {

  def schema = ShowCardsSchema.SCHEMA

  def this() = this(null, null)

  def isMuck = getMuck
}
