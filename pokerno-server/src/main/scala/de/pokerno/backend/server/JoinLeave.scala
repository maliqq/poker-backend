package de.pokerno.backend.server

import akka.actor.ActorLogging
import de.pokerno.model.{Player, Seat, Table}
import de.pokerno.gameplay
import de.pokerno.protocol.cmd

trait JoinLeave { _: ActorLogging =>

  def table: Table
  def events: gameplay.Events

  protected def joinPlayer(join: cmd.JoinPlayer) {
    try {
      table.takeSeat(join.pos, join.player, Option(join.amount))
      events.publish(gameplay.Events.playerJoin(join.pos, join.player, join.amount)) { _.all() }
    } catch {
      case err: Seat.IsTaken        ⇒
        val seat = table.seats(join.pos) 
        log.warning("Can't join player {} at {}: seat is taken ({})", join.player, join.pos, seat)
        
      case err: Table.AlreadyJoined ⇒
        log.warning("Can't join player {} at {}: already joined", join.player, join.pos)
    }
  } 

  protected def leavePlayer(player: Player) {
    table.playerPos(player) map { pos =>
      val seat = table.seats(pos)
      events.publish(gameplay.Events.playerLeave(pos, seat.player.get)) { _.all() } // FIXME unify
      table.clearSeat(pos)
    }
  }

}
