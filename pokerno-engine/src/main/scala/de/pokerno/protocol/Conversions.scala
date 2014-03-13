package de.pokerno.protocol

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.{ model, poker, gameplay }

import com.dyuproject.protostuff.ByteString

object Conversions {

  import CommonConversions._
  import WireConversions._
  import MsgConversions._
  //import RpcConversions._

  implicit def bet2wire(b: model.Bet): wire.Bet = {
    if (b != null) return wire.Bet(b.betType, b.amount match {
      case Some(n) ⇒ n.toDouble
      case None    ⇒ null
    }, b.timeout match {
      case Some(flag) ⇒ flag
      case None       ⇒ null
    })
    return null
  }

  implicit def wire2bet(w: wire.Bet) =
    new model.Bet(w.getType, w.getAmount match {
      case null ⇒ None
      case n    ⇒ Some(n)
    })

  implicit def hand2wire(h: poker.Hand) = new wire.Hand(
    cards = h.cards.value,
    rank = h.rank.get,
    high = h.high,
    value = h.value,
    kicker = h.kicker,
    string = h.description
  )

  import poker.Hand._

  implicit def wire2hand(w: wire.Hand) = new poker.Hand(wire2cards(w.cards),
    rank = Some(w.rank),
    value = w.value,
    High = Left(w.high),
    Kicker = Left(w.kicker)
  )

}

