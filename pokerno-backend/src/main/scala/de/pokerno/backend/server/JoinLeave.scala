package de.pokerno.backend.server

import de.pokerno.protocol.cmd
import de.pokerno.model
import de.pokerno.gameplay
import de.pokerno.protocol.CommonConversions._

trait JoinLeave {
  
  def table: model.Table
  def events: gameplay.Events
  def changeSeatState(player: model.Player, notify: Boolean = true)(f: ((model.Seat, Int)) ⇒ Unit)

  protected def joinPlayer(join: cmd.JoinPlayer) {
    try {
      table.takeSeat(join.pos, join.player, Some(join.amount))
      events.publish(gameplay.Events.joinTable((join.player, join.pos), join.amount))
    } catch {
      case err: model.Seat.IsTaken        ⇒
      case err: model.Table.AlreadyJoined ⇒
    }
  }
  
  protected def leavePlayer(player: model.Player) {
    changeSeatState(player, notify = false) { case box @ (seat, pos) =>
      events.publish(gameplay.Events.leaveTable((seat.player.get, pos))) // FIXME unify
      table.clearSeat(pos)
    }
    table.removePlayer(player)
    //changeSeatState(player) { _._1 clear }
  }
  
}
