package de.pokerno.protocol.commands

import beans._

sealed case class KickPlayer(
  @BeanProperty var player: Player
) extends Command {}
