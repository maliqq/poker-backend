package de.pokerno.protocol.commands

import beans._
import de.pokerno.model.Bet

sealed case class AddBet(
  @BeanProperty var player: Player,
  @BeanProperty var bet: Bet
) extends Command {}
