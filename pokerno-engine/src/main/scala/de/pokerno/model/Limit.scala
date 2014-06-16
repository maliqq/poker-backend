package de.pokerno.model

import com.fasterxml.jackson.annotation.JsonValue

trait Limit {
  @JsonValue def name: String
  
  override def toString = name
  
  def raise(total: Decimal, bb: Decimal, potSize: Decimal): Tuple2[Decimal, Decimal]
}

object Limit {
  case object None extends Limit {
    def name = "no-limit"
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, total)
  }

  case object Fixed extends Limit {
    def name = "fixed-limit"
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, bb)
  }

  case object Pot extends Limit {
    def name = "pot-limit"
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
