package de.pokerno.form.room

import de.pokerno.model.Player
import de.pokerno.model.table.seat.Sitting
import math.{BigDecimal => Decimal}

trait JoinLeave {
  def joinPlayer(player: Player, pos: Int, amount: Option[Decimal])
  def leavePlayer(player: Player)
  def leaveSeat(seat: Sitting)
}
