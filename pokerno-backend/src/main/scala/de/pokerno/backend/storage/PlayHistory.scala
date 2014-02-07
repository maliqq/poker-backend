package de.pokerno.backend.storage

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.poker
import de.pokerno.protocol.msg

object PlayHistory {

  class Entry(
    id: String,
    start: java.util.Date,
    stop: java.util.Date,
    winners: Map[String, Decimal],
    knownCards: Map[String, List[poker.Card]],
    actions: List[msg.Message],
    pot: Decimal,
    rake: Option[Decimal] = None)

}
