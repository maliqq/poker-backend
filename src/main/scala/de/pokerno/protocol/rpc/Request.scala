package de.pokerno.protocol.rpc

import de.pokerno.model
import de.pokerno.protocol.{wire, Message => BaseMessage, HasPlayer}
import wire.Conversions._

import math.{ BigDecimal ⇒ Decimal }
import beans._
import org.msgpack.annotation.{ Message => MsgPack }
import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonSubTypes}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "$method"
)
@JsonSubTypes(Array(
  // create new room
  new JsonSubTypes.Type(value = classOf[CreateRoom], name="CreateRoom")
))
abstract class Request extends BaseMessage {
}

/**
 * Node event
 * */
@MsgPack
sealed case class CreateRoom(
    @BeanProperty
    var id: String,
    @BeanProperty
    var table: wire.Table,
    @BeanProperty
    var variation: wire.Variation,
    @BeanProperty
    var stake: wire.Stake
) extends Request {
  
  def schema = CreateRoomSchema.SCHEMA
  
  def this() = this(null, null, null, null)
  
}

/**
 * Table action
 * */
@MsgPack
sealed case class KickPlayer(
    @BeanProperty
    var player: String,
    @BeanProperty
    var reason: String
) {
  
  def schema = KickPlayerSchema.SCHEMA
  
  def this() = this(null, null)
  
}

@MsgPack
sealed case class AddBet(
    
    var player: model.Player,
    
    var _bet: model.Bet
    
) extends Request with HasPlayer {
  
  def schema = AddBetSchema.SCHEMA
  
  def this() = this(null, null)
  
  def getBet: wire.Bet = if (_bet != null) wire.Bet(_bet.betType, _bet.amount.toDouble)
    else null
  def setBet(v: wire.Bet) = _bet = new model.Bet(v.getType, v.getAmount.toDouble)
}
