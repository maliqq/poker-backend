package de.pokerno.backend.server

import de.pokerno.protocol.cmd
import de.pokerno.model
import de.pokerno.gameplay
import de.pokerno.protocol.CommonConversions._

trait JoinLeave {
  
  def table: model.Table
  def events: gameplay.Events

  protected def joinPlayer(join: cmd.JoinPlayer) {
    try {
      table.addPlayer(join.pos, join.player, Some(join.amount))
      events.publish(gameplay.Events.joinTable((join.player, join.pos), join.amount))
    } catch {
      case err: model.Seat.IsTaken        ⇒
      case err: model.Table.AlreadyJoined ⇒
    }
  }
  
}
