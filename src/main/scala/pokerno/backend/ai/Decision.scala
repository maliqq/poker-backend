package pokerno.backend.ai

import scala.math.{ BigDecimal ⇒ Decimal }

case class Decision(
    val minBet: Decimal = .0,
    val maxBet: Decimal = .0,
    val raiseChance: Double = .0,
    val allInChance: Double = .0) {

  override def toString = "Bet min=%.2f max=%.2f Raise chance=%.2f%% All in chance=%.2f%%".format(minBet, maxBet, raiseChance * 100, allInChance * 100)

}
