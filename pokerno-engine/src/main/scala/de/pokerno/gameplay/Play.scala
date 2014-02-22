package de.pokerno.gameplay

import de.pokerno.poker.Card
import de.pokerno.model.{ Player, Pot }
import math.{ BigDecimal ⇒ Decimal }

class Play(gameplay: Context) {
  val id: String = java.util.UUID.randomUUID().toString()

  // timestamps
  var startAt: java.util.Date = new java.util.Date()
  var finishAt: java.util.Date = null

  def finished() {
    finishAt = new java.util.Date()
  }

  var getStreet: () ⇒ Option[Street.Value] = () ⇒ None
  def street: Option[Street.Value] = getStreet()
  def require = (gameplay.round.call, gameplay.round.raise)
  def acting = gameplay.round.box
  def pot = gameplay.round.pot
  def rake: Option[Decimal] = None
  val winners: Map[Player, Decimal] = Map.empty
  val knownCards: Map[Player, List[Card]] = Map.empty
}
