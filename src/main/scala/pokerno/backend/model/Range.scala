package pokerno.backend.model

import scala.math.{BigDecimal => Decimal}

object Range {
  class Disabled(amount: Decimal) extends Error("amount=%.2f".format(amount))
  class GreaterThanMax(amount: Decimal, max: Decimal) extends Error("amount=%.2f max=%.2f".format(amount, max))
  class LessThanMin(amount: Decimal, min: Decimal) extends Error("amount=%2.f min=%.2f".format(amount, min))
  
  def apply(min: Decimal, max: Decimal): Range = new Range((min, max))
}

case class Range(val value: Tuple2[Decimal, Decimal]) {
  def min = value._1
  def max = value._2
  
  def validate(amount: Decimal, allIn: Boolean = false) {
    if (max == .0)
      throw new Range.Disabled(amount)
    
    if (amount > max)
      throw new Range.GreaterThanMax(amount, max)
    
    if (amount < min && !allIn)
      throw new Range.LessThanMin(amount, min)
  }
}
