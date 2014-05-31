package de.pokerno.model

import beans._
import math.{ BigDecimal ⇒ Decimal }

object MinMax {
  case class GreaterThanMax(amount: Decimal, max: Decimal)
    extends Error("amount=%.2f max=%.2f" format (amount, max))

  case class LessThanMin(amount: Decimal, min: Decimal)
    extends Error("amount=%.2f min=%.2f" format (amount, min))
}

case class MinMax[T](
    @BeanProperty val min: T,
    @BeanProperty val max: T)
