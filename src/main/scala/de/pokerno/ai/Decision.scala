package de.pokerno.ai

import scala.math.{ BigDecimal ⇒ Decimal }

case class Decision(
    val minBet: Decimal = .0,
    val maxBet: Decimal = .0,
    val raiseChance: Double = .0,
    val allInChance: Double = .0) {

  override def toString = {
    var s = new StringBuilder
    s.append("Bet min=%.2f max=%.2f" format(minBet, maxBet))
    s.append("Raise chance=%.2f%%" format(raiseChance * 100))
    s.append("All-in chance=%.2f%%" format(allInChance * 100))
    s.toString
  }
  
}
