package de.pokerno.gameplay.discarding

import org.slf4j.LoggerFactory
import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonInclude, JsonGetter}
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import math.{ BigDecimal ⇒ Decimal }

import de.pokerno.poker.Cards
import de.pokerno.model.{Table, Game, Player, Dealer, seat}

class Round(table: Table, game: Game, dealer: Dealer) {

  private val log = LoggerFactory.getLogger(getClass)
  
  private var _seats = table.sittingFromButton
  def seats = _seats
  
  // current discarding
  private var _discarding: Option[seat.Sitting] = None
  def discarding = _discarding
  
  @JsonGetter def current = discarding map(_.pos)
  
  def reset() {
    _discarding = None
    _seats = table.sittingFromButton
  }
  
  def discard(sitting: seat.Sitting, cards: Cards) = {
    val newCards = dealer.discard(cards, sitting.player)
    newCards
  }
  
}
