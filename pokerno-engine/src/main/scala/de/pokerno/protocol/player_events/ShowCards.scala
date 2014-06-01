package de.pokerno.protocol.player_events

import beans._

sealed case class ShowCards(
  @BeanProperty var cards: Cards = null
) extends PlayerEvent {}
