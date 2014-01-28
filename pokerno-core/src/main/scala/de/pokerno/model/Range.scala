package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

object Range {
  case class GreaterThanMax(amount: Decimal, max: Decimal)
    extends Error("amount=%.2f max=%.2f" format (amount, max))

  case class LessThanMin(amount: Decimal, min: Decimal)
    extends Error("amount=%.2f min=%.2f" format (amount, min))

  def apply(min: Decimal, max: Decimal): Range = new Range((min, max))

  implicit def tuple2Range(t: Tuple2[Double, Double]) = new Range((t._1, t._2))
  implicit def range2Tuple(r: Range): Tuple2[Decimal, Decimal] = r.value
}

case class Range(value: Tuple2[Decimal, Decimal] = (.0, .0)) {
  def min = value._1
  def max = value._2

  def isValid(amount: Decimal, available: Decimal) = amount <= max && (amount >= min || amount == available)
}
