package de.pokerno.model

import de.pokerno.poker.{Card, Cards}
import math.{ BigDecimal ⇒ Decimal }
import collection.mutable.ListBuffer
import de.pokerno.protocol.Serializers.Cards2Binary
import com.fasterxml.jackson.annotation.{JsonGetter, JsonProperty, JsonInclude}
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration

@JsonInclude(JsonInclude.Include.NON_NULL)
case class Play(val id: String = java.util.UUID.randomUUID().toString()) {
  // timestamps
  val started: java.util.Date = new java.util.Date()
  
  private var _ended: java.util.Date = null
  @JsonGetter def ended = _ended
  def end(): Unit = _ended = new java.util.Date()
  
  private var _street: Option[Street.Value] = None
  def street_=(v: Street.Value): Unit = _street = Some(v)
  @JsonScalaEnumeration(classOf[StreetRef]) @JsonProperty def street: Street.Value = _street orNull // FIXME

  val actions = collection.mutable.Map[Street.Value, ListBuffer[Action]]().withDefaultValue(ListBuffer.empty)
  def action(player: Player, bet: Bet) {
    actions(street) += Action(player, bet)
  }
  
  @JsonSerialize(converter=classOf[Cards2Binary]) var board: Cards = ListBuffer[Card]()
  
  val winners = collection.mutable.Map[Player, Decimal]().withDefaultValue(0)
  def winner(player: Player, amount: Decimal) {
    winners(player) += amount
  }
  
  val knownCards = collection.mutable.Map[Player, Cards]()
  def show(player: Player, cards: Cards) {
    knownCards(player) = cards
  }
}
