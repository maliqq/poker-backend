package de.pokerno.protocol.rpc

import de.pokerno.{model, poker}
import de.pokerno.protocol.{wire, Message => BaseMessage, HasPlayer, HasAmount, HasCards}
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
sealed case class JoinPlayer(
    @BeanProperty
    var pos: Integer,
    var player: model.Player,
    var amount: Decimal) extends Request with HasAmount with HasPlayer {
  
  def schema = JoinPlayerSchema.SCHEMA
  //def pipeSchema = JoinTableSchema.PIPE_SCHEMA
  
  def this() = this(null, null, null)
}

@MsgPack
sealed case class KickPlayer(
    var player: model.Player,
    @BeanProperty
    var reason: String
) extends Request with HasPlayer {
  
  def schema = KickPlayerSchema.SCHEMA
  
  def this() = this(null, null)
  
}

@MsgPack
sealed case class Chat(
    var player: model.Player
) extends Request with HasPlayer {
  
  def schema = ChatSchema.SCHEMA
  
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

@MsgPack
sealed case class DealCards(
    @BeanProperty
    var `type`: model.DealCards.Value,
    
    var cards: List[poker.Card] = List.empty,
    
    var player: model.Player = null,
    
    @BeanProperty
    var cardsNum: Integer = null
) extends Request with HasPlayer with HasCards {
  
  def schema = DealCardsSchema.SCHEMA
  
}

@MsgPack
sealed case class DiscardCards(
    var cards: List[poker.Card] = null,
    
    var player: model.Player = null

) extends Request with HasPlayer with HasCards {
  
  def schema = DealCardsSchema.SCHEMA
  
}

@MsgPack
sealed case class ShowCards(
    var cards: List[poker.Card] = null,
    
    var player: model.Player = null,
    
    @BeanProperty
    var muck: java.lang.Boolean = null

) extends Request with HasPlayer with HasCards {
  
  def schema = ShowCardsSchema.SCHEMA
  
}
