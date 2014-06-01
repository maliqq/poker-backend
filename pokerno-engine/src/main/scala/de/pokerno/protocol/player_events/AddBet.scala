package de.pokerno.protocol.player_events

import beans._
import de.pokerno.model.Bet

sealed case class AddBet(
  @BeanProperty var bet: Bet = null
) extends PlayerEvent {}
