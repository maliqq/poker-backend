package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

import com.fasterxml.jackson.annotation.JsonValue

abstract class Limit(@JsonValue val name: String) {
  override def toString = name
    
  def raise(total: Decimal, bb: Decimal, potSize: Decimal): Tuple2[Decimal, Decimal]
}

object Limit {
  case object None extends Limit("no-limit") {
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, total)
  }

  case object Fixed extends Limit("fixed-limit") {
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, bb)
  }

  case object Pot extends Limit("pot-limit") {
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, potSize)
  }
  
  implicit def string2limit(v: String): Limit = v match {
    case "no-limit" | "nolimit" | "no" ⇒
      None
    case "fixed-limit" | "fixedlimit" | "fixed" ⇒
      Fixed
    case "pot-limit" | "potlimit" | "pot" ⇒
      Pot
    case _ ⇒
      null // throw?
  }
}
