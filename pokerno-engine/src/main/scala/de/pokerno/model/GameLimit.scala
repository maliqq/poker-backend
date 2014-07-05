package de.pokerno.model

import com.fasterxml.jackson.annotation.JsonValue

trait GameLimit {
  @JsonValue def name: String
  
  override def toString = name
  
  def raise(total: Decimal, bb: Decimal, potSize: Decimal): Tuple2[Decimal, Decimal]
}

object GameLimit {
  case object None extends GameLimit {
    def name = "no-limit"
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, total)
  }

  case object Fixed extends GameLimit {
    def name = "fixed-limit"
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, bb)
  }

  case object Pot extends GameLimit {
    def name = "pot-limit"
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, List(potSize, bb).max)
  }
  
  implicit def string2limit(v: String): GameLimit = v match {
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
