package de.pokerno.protocol.commands

import beans._

sealed case class Chat(
  @BeanProperty var player: Player,
  @BeanProperty var message: String
) extends Command {}
