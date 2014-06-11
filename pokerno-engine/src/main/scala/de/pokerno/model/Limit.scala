package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

import com.fasterxml.jackson.annotation.JsonValue

trait Limit {
  def raise(total: Decimal, bb: Decimal, potSize: Decimal): Tuple2[Decimal, Decimal]
}

object Limit {
  case object None extends Limit {
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, total)
    @JsonValue override def toString = "no-limit"
  }

  case object Fixed extends Limit {
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, bb)
    @JsonValue override def toString = "fixed-limit"
  }

  case object Pot extends Limit {
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, potSize)
    @JsonValue override def toString = "pot-limit"
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
