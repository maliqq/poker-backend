package de.pokerno.form.cash

import de.pokerno.model.{Player, Stake, Table}
import de.pokerno.model.table.seat.Sitting
import de.pokerno.model.table.Seat
import de.pokerno.gameplay
import de.pokerno.protocol.cmd
import de.pokerno.form.{Room, CashRoom}
import math.{BigDecimal => Decimal}

trait JoinLeave extends de.pokerno.form.room.JoinLeave { room: CashRoom =>
  
  def joinPlayer(player: Player, pos: Int, amount: Option[Decimal]) {
    reserveSeat(pos, player) match {
      case Some(seat) =>
        if (amount.isDefined) {
          buyInSeat(seat, amount.get)
        } else {
          askBuyIn(seat)
          // TODO notify seat reserved
        }
        
      case _ =>
    }
  }
  
  protected def joinPlayer(join: cmd.JoinPlayer) {
    val cmd.JoinPlayer(pos, player, _amount) = join
    
    joinPlayer(player, pos, Option(_amount))
  }
  
  protected def reserveSeat(pos: Int, player: Player): Option[Sitting] = {
    try {
      val seat = table.take(pos, player)
      return Some(seat)
    } catch {
      case err: Seat.IsTaken ⇒
        val seat = table.seats(pos) 
        log.warning("Can't join player {} at {}: seat is taken ({})", player, pos, seat)
        events.publish(gameplay.Events.error(err)) { _.one(player) }
      
      case err: Table.AlreadyJoined ⇒
        log.warning("Can't join player {} at {}: already joined", player, pos)
        events.publish(gameplay.Events.error(err)) { _.one(player) }
    }
    None
  }
  
  protected def askBuyIn(seat: Sitting) {
    val player = seat.player
    // ask buy in
    val (requiredMin, requiredMax) = stake.buyInAmount
    balance.availableWithBonus(player, requiredMax.toDouble).onSuccess { amount =>
      if (amount < requiredMin) {
        table.clear(seat.pos)
      }
      events.publish(gameplay.Events.requireBuyIn(seat, stake, amount)) { _.one(player) }
    }
  }
  
  protected def buyInSeat(seat: Sitting, amount: Decimal) {
    val player = seat.player
    val f = balance.join(roomId, player, amount.toDouble)
    f.onSuccess { _ =>
      // TODO: reserve seat first 
      seat.buyIn(amount)
      events broadcast gameplay.Events.playerJoin(seat)
    }
    
    f.onFailure {
      case err: de.pokerno.payment.thrift.NotEnoughMoney =>
        log.error("balance error: {}", err.message)
        events.publish(gameplay.Events.error(err)) { _.one(player) }
      
      case err: de.pokerno.payment.thrift.BuyInRequired =>
        log.error("balance error: {}", err.message)
        events.publish(gameplay.Events.error(err)) { _.one(player) }
    }
  }
  
  def leaveSeat(seat: Sitting) {
    table.clear(seat.pos)
    if (seat.stackAmount > 0) {
      balance.leave(roomId, seat.player, seat.stackAmount.toDouble)
    }
    events broadcast gameplay.Events.playerLeave(seat)
  }
  
  def leavePlayer(player: Player) {
    table(player) map { seat =>
      if (seat.notActive || notRunning) {
        leaveSeat(seat)
      } else {
        seat.leaving
        running map { case Room.Running(ctx, ref) =>
          ref ! gameplay.Betting.Cancel(player)
        }
      }
    }
  }

}
