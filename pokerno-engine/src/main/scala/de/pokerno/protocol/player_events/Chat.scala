package de.pokerno.protocol.player_events

import beans._

sealed case class Chat(
  @BeanProperty var message: String = null
) extends PlayerEvent {}
