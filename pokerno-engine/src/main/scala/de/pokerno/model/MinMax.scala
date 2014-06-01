package de.pokerno.model

import beans._
import math.{ BigDecimal ⇒ Decimal }

object MinMax {
  implicit def tuple2minmax[T <: Double](tuple: Tuple2[T, T]): MinMax[Decimal] =
    MinMax(tuple._1, tuple._2)
  
  case class GreaterThanMax(amount: Decimal, max: Decimal)
    extends Error("amount=%.2f max=%.2f" format (amount, max))

  case class LessThanMin(amount: Decimal, min: Decimal)
    extends Error("amount=%.2f min=%.2f" format (amount, min))
}

case class MinMax[T](
    @BeanProperty val min: T,
    @BeanProperty val max: T)
