package de.pokerno.model

import de.pokerno.poker.Card
import math.{ BigDecimal ⇒ Decimal }
import com.fasterxml.jackson.annotation.{JsonProperty, JsonInclude}
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration

@JsonInclude(JsonInclude.Include.NON_NULL)
case class Play(val id: String = java.util.UUID.randomUUID().toString()) {
  // timestamps
  val started: java.util.Date = new java.util.Date()
  
  private var _ended: java.util.Date = null
  @JsonProperty def ended = _ended
  def end(): Unit = _ended = new java.util.Date()
  
  private var _street: Option[Street.Value] = None
  def street_=(v: Street.Value): Unit = _street = Some(v)
  @JsonScalaEnumeration(classOf[StreetRef]) @JsonProperty def street: Street.Value = _street orNull // FIXME

  var winners: Map[Player, Decimal] = Map.empty
  var knownCards: Map[Player, List[Card]] = Map.empty
}
