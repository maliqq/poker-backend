package de.pokerno.protocol.msg

import de.pokerno.protocol.{wire, Message => BaseMessage}

import com.dyuproject.protostuff
import com.dyuproject.protostuff.ByteString
import beans._
import org.msgpack.annotation.{ Message => MsgPack }
import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonSubTypes}

abstract class Inbound extends Message

/**
 * Action event
 * */
@MsgPack
sealed case class AddBet(
    @BeanProperty
    var bet: wire.Bet
  ) extends Message {
  def schema = AddBetSchema.SCHEMA
  
  def this() = this(null)
}

@MsgPack
sealed case class DiscardCards(
    @BeanProperty
    var cards: ByteString
  ) extends Message {
  def schema = DiscardCardsSchema.SCHEMA
  
  @BeanProperty
  var cardsNum: Integer = null
  
  @BeanProperty
  var `type`: DiscardCardsSchema.DiscardType = null

  def this() = this(null)
  
}

@MsgPack
sealed case class ShowCards(
    @BeanProperty
    var cards: ByteString,
    @BeanProperty
    var muck: java.lang.Boolean = null) extends Message {
  
  def schema = ShowCardsSchema.SCHEMA
  
  @BeanProperty
  var `type`: ShowCardsSchema.ShowType = if (muck)
    ShowCardsSchema.ShowType.MUCK
  else
    ShowCardsSchema.ShowType.SHOW

  def this() = this(null)
  
}
