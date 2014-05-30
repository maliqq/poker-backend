package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

object MinMax {
  case class GreaterThanMax(amount: Decimal, max: Decimal)
    extends Error("amount=%.2f max=%.2f" format (amount, max))

  case class LessThanMin(amount: Decimal, min: Decimal)
    extends Error("amount=%.2f min=%.2f" format (amount, min))

  def apply(min: Decimal, max: Decimal): MinMax = MinMax(min, max)
  
  implicit def wrapminmax(value: MinMax): MinMaxWrapper = MinMaxWrapper(value)
  implicit def unwrapminmax(wrap: MinMaxWrapper): MinMax = wrap.value
}

case class MinMaxWrapper(underlying: MinMax) {
  def value = underlying
  def min = underlying._1
  def max = underlying._2
}
