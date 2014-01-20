package de.pokerno.protocol

import math.{BigDecimal => Decimal}
import de.pokerno.{model, poker}

trait ProtobufMessage {
  import com.dyuproject.protostuff.{Schema, ByteString}
  implicit def schemaConv(s: Schema[_ <: Any]): Schema[Any] = s.asInstanceOf[Schema[Any]]
  
  implicit def byteArray2ByteString(v: Array[Byte]) =
    ByteString.copyFrom(v)
  
  def schema: Schema[Any]
  //def pipeSchema: protostuff.Pipe.Schema[_]
}

abstract class Message extends ProtobufMessage

trait HasPlayer {
  def player: model.Player
  def player_=(v: model.Player)
  
  def getPlayer: String = if (player != null) player.id else null
  def setPlayer(v: String) = player = new model.Player(v)
}

trait HasBet {
  def bet: model.Bet
  def bet_=(v: model.Bet)
  
  import wire.Conversions._
  
  def getBet: wire.Bet = if (bet != null) wire.Bet(bet.betType, bet.amount.toDouble)
    else null
  def setBet(v: wire.Bet) = bet = new model.Bet(v.getType, v.getAmount.toDouble)
}

trait HasAmount {
  def amount: Decimal
  def amount_=(v: Decimal)
  
  def getAmount: java.lang.Double = if (amount != null) amount.toDouble else null
  def setAmount(v: java.lang.Double) = amount = v.toDouble
}

trait HasCards {
  import com.dyuproject.protostuff.ByteString
  
  implicit def byteString2Cards(v: ByteString): List[poker.Card] = v.toByteArray.map(poker.Card.wrap(_)).toList
  implicit def cards2ByteString(v: List[poker.Card]): ByteString = ByteString.copyFrom(v.map(_.toByte).toArray)
  
  def cards: List[poker.Card]
  def cards_=(v: List[poker.Card])
  
  def getCards: ByteString = cards
  def setCards(v: ByteString) = cards = v 
}
