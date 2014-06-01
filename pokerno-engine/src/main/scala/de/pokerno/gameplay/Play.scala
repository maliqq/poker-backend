package de.pokerno.gameplay

import de.pokerno.poker.Card
import de.pokerno.model.{ Player, Pot, Street }
import math.{ BigDecimal ⇒ Decimal }

class Play() {
  val id: String = java.util.UUID.randomUUID().toString()
  
  // timestamps
  val started: java.util.Date = new java.util.Date()
  
  private var _ended: java.util.Date = null
  def ended = _ended
  def end(): Unit = _ended = new java.util.Date()

  var winners: Map[Player, Decimal] = Map.empty
  var knownCards: Map[Player, List[Card]] = Map.empty
}
