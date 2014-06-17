package de.pokerno.backend.server

import akka.actor.ActorLogging
import de.pokerno.model.{Player, Seat, Table}
import de.pokerno.gameplay
import de.pokerno.protocol.cmd
import concurrent.Await

trait JoinLeave { room: Room =>

  def table: Table
  def events: gameplay.Events
  
  protected def joinPlayer(join: cmd.JoinPlayer) {
    val cmd.JoinPlayer(pos, player, amount) = join
    val f = balance.withdraw(player, amount.toDouble)
    f.onSuccess { _ =>
      try {
        val seat = table.takeSeat(join.pos, join.player, Option(amount))
        events broadcast gameplay.Events.playerJoin(seat)
      } catch {
        case err: Seat.IsTaken ⇒
          val seat = table.seats(join.pos) 
          log.warning("Can't join player {} at {}: seat is taken ({})", player, pos, seat)
          
        case err: Table.AlreadyJoined ⇒
          log.warning("Can't join player {} at {}: already joined", player, pos)
      }
    }
    f.onFailure {
      case cause: Throwable =>
        log.error("")
    }
  }
  
  protected def leavePlayer(player: Player) {
    table.playerSeat(player) map { seat =>
      if (seat.canLeave || notRunning) {
        events broadcast gameplay.Events.playerLeave(seat)
        table.clearSeat(seat.pos)
        balance.advance(seat.player, seat.stackAmount.toDouble)
      } else running.map { case Running(ctx, ref) =>
        ref ! gameplay.Betting.Cancel(player)
      }
    }
  }

}
