package de.pokerno.protocol.msg

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.{poker, model, gameplay}
import de.pokerno.protocol.{wire, Message => BaseMessage, HasPlayer, HasCards, HasAmount, HasBet}
import wire.Conversions._
import Conversions._

import com.dyuproject.protostuff
import beans._
import org.msgpack.annotation.{ Message => MsgPack }
import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonSubTypes}

abstract class Inbound extends Message

/**
 * Action event
 * */
@MsgPack
sealed case class AddBet(var bet: model.Bet) extends Message with HasBet {
  def schema = AddBetSchema.SCHEMA
  
  def this() = this(null)
}

@MsgPack
sealed case class DiscardCards(var cards: List[poker.Card]) extends Message with HasCards {
  def schema = DiscardCardsSchema.SCHEMA
  
  @BeanProperty
  var cardsNum: Integer = null
  
  @BeanProperty
  var `type`: DiscardCardsSchema.DiscardType = null

  def this() = this(null)
  
}

@MsgPack
sealed case class ShowCards(
    var cards: List[poker.Card],
    @BeanProperty
    var muck: java.lang.Boolean = null) extends Message with HasCards {
  
  def schema = ShowCardsSchema.SCHEMA
  
  @BeanProperty
  var `type`: ShowCardsSchema.ShowType = if (muck)
    ShowCardsSchema.ShowType.MUCK
  else
    ShowCardsSchema.ShowType.SHOW

  def this() = this(null)
  
}
