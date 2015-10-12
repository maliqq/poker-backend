package de.pokerno.model

import collection.mutable.ListBuffer

import de.pokerno.poker.{Card, Cards}
import de.pokerno.model.seat.impl.Sitting
import de.pokerno.protocol.Serializers.Cards2Binary

import com.fasterxml.jackson.annotation.{JsonGetter, JsonProperty, JsonInclude, JsonIgnore}
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration

@JsonInclude(JsonInclude.Include.NON_NULL)
case class Play(val id: java.util.UUID = java.util.UUID.randomUUID(), @JsonIgnore val dealer: Dealer = new Dealer) {
  // timestamps
  @JsonProperty("start_at") val startAt: java.util.Date = new java.util.Date()

  private var _stopAt: java.util.Date = null
  @JsonGetter("stop_at") def stopAt = _stopAt
  def stop(): Unit = {
    _street = None
    _stopAt = new java.util.Date()
  }

  // button position (saved after play stops)
  private var _button: Option[Int] = None
  @JsonGetter("button") def button = _button
  def button_=(pos: Int) = _button = Some(pos)

  // current board
  @JsonSerialize(converter=classOf[Cards2Binary]) def board = dealer.board

  // pot, rake, uncalled
  @JsonProperty val pot = new Pot
  @JsonProperty val rake: Option[SidePot] = None
  @JsonProperty var uncalled: Decimal = 0

  // current street
  private var _street: Option[Street.Value] = None
  def street_=(v: Street.Value): Unit = _street = Some(v)
  @JsonScalaEnumeration(classOf[StreetRef]) @JsonProperty def street: Street.Value = _street orNull // FIXME

  // actions list by street
  private val _actions = collection.mutable.Map[Street.Value, ListBuffer[Action]]()
  def actions = _actions
  def action(a: Action) {
    if (!_actions.contains(street))
      _actions.put(street, ListBuffer.empty) // .withDefault didn't work
    _actions(street) += a
  }

  // seating
  private val _seating = collection.mutable.Map[Player, Int]().withDefaultValue(0)
  def seating = _seating

  // stacks
  private val _stacks = collection.mutable.Map[Player, Decimal]().withDefaultValue(0)
  def stacks = _stacks

  def sit(seat: Sitting) {
    _seating.put(seat.player, seat.pos)
    _stacks.put(seat.player, seat.stackAmount)
  }

  // winners, losers
  val net = collection.mutable.Map[Player, Decimal]().withDefaultValue(0)
  def winner(player: Player, amount: Decimal) {
    net(player) += amount
  }
  def loser(player: Player, amount: Decimal) {
    net(player) -= amount
  }

  // known cards(shown)
  @JsonProperty("known_cards") val knownCards = collection.mutable.Map[Player, Cards]()
  def show(player: Player, cards: Cards) {
    knownCards(player) = cards
  }
}
