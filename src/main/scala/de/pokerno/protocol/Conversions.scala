package de.pokerno.protocol

import math.{BigDecimal => Decimal}
import de.pokerno.{model, poker}

import com.dyuproject.protostuff.ByteString

object Conversions {

  import wire.Conversions._
  import msg.Conversions._
  
  implicit def player2wire(p: model.Player): String = {
    if (p != null) return p.id
    return null
  }

  implicit def wire2player(w: String): model.Player =
    new model.Player(w)


  implicit def bet2wire(b: model.Bet): wire.Bet = {
    if (b != null) return wire.Bet(b.betType, b.amount.toDouble)
    return null
  }

  implicit def wire2bet(w: wire.Bet) =
    new model.Bet(w.getType, w.getAmount)

  implicit def decimal2wire(d: Decimal): java.lang.Double = {
    if (d != null) return d.toDouble
    return null
  }

  implicit def wire2decimal(w: java.lang.Double): Decimal = w:Decimal
  
  implicit def cards2wire(c: List[poker.Card]): ByteString =
    ByteString.copyFrom(c.map(_.toByte).toArray)
  
  implicit def wire2cards(w: ByteString): List[poker.Card] =
    w.toByteArray.map(poker.Card.wrap(_)).toList
  
  implicit def hand2wire(h: poker.Hand) = new wire.Hand(
      cards = h.cards.value,
      rank = h.rank.get,
      high = h.high,
      value = h.value,
      kicker = h.kicker,
      string = h.description
    )
  
  implicit def wire2hand(w: wire.Hand) = new poker.Hand(
      cards = new poker.Hand.Cards(w.cards),
      rank = Some(w.rank),
      value = w.value,
      High = Left(w.high),
      Kicker = Left(w.kicker)
    )

}