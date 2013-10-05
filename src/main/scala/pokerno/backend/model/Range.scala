package pokerno.backend.model

import scala.math.{BigDecimal => Decimal}

object Range {
  case class Disabled(amount: Decimal)
    extends Error("amount=%.2f".format(amount))
  
  case class GreaterThanMax(amount: Decimal, max: Decimal)
    extends Error("amount=%.2f max=%.2f".format(amount, max))
  
  case class LessThanMin(amount: Decimal, min: Decimal)
    extends Error("amount=%2.f min=%.2f".format(amount, min))
  
  def apply(min: Decimal, max: Decimal): Range = new Range((min, max))
}

case class Range(val value: Tuple2[Decimal, Decimal]) {
  def min = value._1
  def max = value._2
  
  def validate(amount: Decimal, allIn: Boolean = false) {
    if (max == .0)
      throw Range.Disabled(amount)
    
    if (amount > max)
      throw Range.GreaterThanMax(amount, max)
    
    if (amount < min && !allIn)
      throw Range.LessThanMin(amount, min)
  }
}
