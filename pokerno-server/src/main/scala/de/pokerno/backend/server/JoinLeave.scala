package de.pokerno.backend.server

import de.pokerno.model.{Player, Seat, Table}
import de.pokerno.gameplay
import de.pokerno.protocol.cmd

trait JoinLeave {

  def table: Table
  def events: gameplay.Events

  protected def joinPlayer(join: cmd.JoinPlayer) {
    try {
      table.takeSeat(join.pos, join.player, Option(join.amount))
      events.publish(gameplay.Events.playerJoin(join.pos, join.player, join.amount)) { _.all() }
    } catch {
      case err: Seat.IsTaken        ⇒
      case err: Table.AlreadyJoined ⇒
    }
  } 

  protected def leavePlayer(player: Player) {
    table.playerPos(player) map { pos =>
      val seat = table.seats(pos)
      events.publish(gameplay.Events.playerLeave(pos, seat.player.get)) { _.all() } // FIXME unify
      table.clearSeat(pos)
    }
    table.removePlayer(player)
  }

}
