package de.pokerno.form.room

import de.pokerno.model.Player
import de.pokerno.model.seat.impl.Sitting
import math.{BigDecimal => Decimal}

trait JoinLeave {
  def joinPlayer(player: Player, pos: Int, amount: Option[Decimal])
  def leavePlayer(player: Player, kick: Boolean = false)
  def leaveSeat(seat: Sitting, kick: Boolean = false)
}
