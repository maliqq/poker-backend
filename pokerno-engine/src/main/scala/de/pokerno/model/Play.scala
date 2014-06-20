package de.pokerno.model

import de.pokerno.poker.{Card, Cards}
import collection.mutable.ListBuffer
import de.pokerno.protocol.Serializers.Cards2Binary
import com.fasterxml.jackson.annotation.{JsonGetter, JsonProperty, JsonInclude}
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration

@JsonInclude(JsonInclude.Include.NON_NULL)
case class Play(val id: java.util.UUID = java.util.UUID.randomUUID()) {
  // timestamps
  val started: java.util.Date = new java.util.Date()
  
  private var _ended: java.util.Date = null
  @JsonGetter def ended = _ended
  def end(): Unit = _ended = new java.util.Date()
  
  private var _street: Option[Street.Value] = None
  def street_=(v: Street.Value): Unit = _street = Some(v)
  @JsonScalaEnumeration(classOf[StreetRef]) @JsonProperty def street: Street.Value = _street orNull // FIXME

  private val _actions = collection.mutable.Map[Street.Value, ListBuffer[Action]]()
  def actions = _actions
  def action(player: Player, bet: Bet) {
    if (!_actions.contains(street))
      _actions.put(street, ListBuffer.empty) // .withDefault didn't work
    _actions(street) += Action(player, bet)
  }
  
  @JsonSerialize(converter=classOf[Cards2Binary]) var board: Cards = ListBuffer[Card]()
  
  private var _button: Int = 0
  
  def button = _button
  def button_=(pos: Int) = _button = pos
  
  private val _seating = new java.util.HashMap[Player, Int]()
  def seating = _seating
  
  private val _stacks = new java.util.HashMap[Player, java.lang.Double]()
  def stacks = _stacks
  
  def sit(sitting: seat.Sitting) {
    _seating.put(sitting.player, sitting.pos)
    _stacks.put(sitting.player, sitting.stackAmount.toDouble)
  }
  
  val net = new java.util.HashMap[Player, java.lang.Double]()
  def winner(player: Player, amount: Decimal) {
    val old = net.getOrDefault(player, 0)
    net.put(player, old + amount.toDouble)
  }
  def loser(player: Player, amount: Decimal) {
    val old = net.getOrDefault(player, 0)
    net.put(player, old - amount.toDouble)
  }
  
  val knownCards = new java.util.HashMap[Player, java.nio.ByteBuffer]()
  def show(player: Player, cards: Cards) {
    knownCards.put(player, java.nio.ByteBuffer.wrap(cards: Array[Byte]))
  }
}
