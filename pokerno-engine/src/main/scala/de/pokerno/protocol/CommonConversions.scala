package de.pokerno.protocol

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.{ model, poker, gameplay }

import com.dyuproject.protostuff.ByteString

object CommonConversions {
  implicit def decimal2wire(d: Decimal): java.lang.Double = d.toDouble

  implicit def wire2decimal(w: java.lang.Double): Decimal = Decimal.double2bigDecimal(w)

  implicit def player2wire(p: model.Player): String = {
    if (p != null) return p.id
    return null
  }

  implicit def wire2player(w: String): model.Player =
    new model.Player(w)

  import model.MinMax._
  
  implicit def minmax2wire(r: model.MinMax) = new wire.MinMax(r.min.toDouble, r.max.toDouble)

  implicit def wire2minmax(w: wire.MinMax) = model.MinMax(w.min, w.max)

  implicit def cards2wire(c: List[poker.Card]): ByteString =
    ByteString.copyFrom(c.map(_.toByte).toArray)

  implicit def wire2cards(w: ByteString): List[poker.Card] =
    w.toByteArray.map(poker.Card.wrap(_)).toList

}
