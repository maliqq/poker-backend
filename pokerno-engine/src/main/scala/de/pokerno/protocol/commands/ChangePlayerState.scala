package de.pokerno.protocol.commands

import beans._
import de.pokerno.model.Seat

sealed case class ChangePlayerState(
  @BeanProperty var player: Player,
  @BeanProperty var state: Seat.State.Value
) extends Command {}
