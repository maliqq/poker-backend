package de.pokerno.model.tournament

import math.{BigDecimal => Decimal}

case class Entry(var stack: Decimal) {
  var rebuysCount: Int = 0
  var addon: Boolean = false
  var knockoutsCount: Int = 0
}
