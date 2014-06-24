package de.pokerno.backend.server.room

import de.pokerno.model.{Player, Stake, Seat, seat, Table}
import de.pokerno.gameplay
import de.pokerno.protocol.cmd
import de.pokerno.backend.server.{Room, Running}

trait JoinLeave { room: Room =>

  def table: Table
  def stake: Stake
  def events: gameplay.Events
  
  protected def joinPlayer(join: cmd.JoinPlayer) {
    val cmd.JoinPlayer(pos, player, amount) = join
    val buyInAmount = Option(amount)
    
    def reserve(): Option[seat.Sitting] = {
      try {
        val seat = table.takeSeat(join.pos, join.player)
        return Some(seat)
      } catch {
        case err: Seat.IsTaken ⇒
          val seat = table.seats(join.pos) 
          log.warning("Can't join player {} at {}: seat is taken ({})", player, pos, seat)
          events broadcast gameplay.Events.error(err)
        case err: Table.AlreadyJoined ⇒
          log.warning("Can't join player {} at {}: already joined", player, pos)
          events broadcast gameplay.Events.error(err)
      }
      None
    }
    
    reserve() match {
      case Some(sitting) =>
        if (buyInAmount.isDefined) {
          val f = balance.join(player, amount.toDouble, roomId)
          f.onSuccess { _ =>
            // TODO: reserve seat first 
            sitting.buyIn(amount)
            events broadcast gameplay.Events.playerJoin(sitting)
          }
          
          f.onFailure { case err: de.pokerno.payment.thrift.Error =>
            log.error("balance error: %s", err.message)
            events broadcast gameplay.Events.error(err)
          }
        } else {
          balance.available(player).onSuccess { amount =>
            events broadcast gameplay.Events.requireBuyIn(sitting, stake, amount)
          }
        }
        
      case _ => 
    }
  }
  
  protected def leavePlayer(player: Player) {
    table.playerSeat(player) map { seat =>
      if (!seat.canLeave) {
        running.map { case Running(ctx, ref) =>
          ref ! gameplay.Betting.Cancel(player)
        }
      }
      table.clearSeat(seat.pos)
      balance.leave(seat.player, seat.stackAmount.toDouble, roomId)
      events broadcast gameplay.Events.playerLeave(seat)
    }
  }

}
