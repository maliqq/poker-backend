package de.pokerno.form.cash

import de.pokerno.model.{Player, Stake, Table}
import de.pokerno.model.seat.impl.Sitting
import de.pokerno.model.Seat
import de.pokerno.gameplay
import de.pokerno.protocol.{cmd, err}
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
      case _: Seat.IsTaken ⇒
        val seat = table.seats(pos)
        val e = new err.Table.Seat.Taken(seat.pos, seat.player)
        log.warning(e.message)
        events.one(player).publish(e)

      case err: Table.AlreadyJoined ⇒
        val e = new err.Table.Player.AlreadyJoined(pos, player)
        log.warning(e.message)
        events.one(player).publish(e)
    }
    None
  }

  protected def askBuyIn(seat: Sitting) {
    val player = seat.player
    // ask buy in
    val (min, max) = stake.buyInAmount
    val f = balance.availableWithBonus(player, min.toDouble)
    f.onSuccess { amount =>
      if (amount < min) {
        table.clear(seat.pos)
        val e = new err.BuyIn.Balance.NotEnoughToBuyIn(player, amount, min)
        events.one(player).publish(e)
      } else {
        events.one(player).publish(gameplay.Events.requireBuyIn(seat, stake, amount))
      }
    }
  }

  protected def askRebuy(seat: Sitting) {
    val player = seat.player

    val (min, max) = stake.buyInAmount
    val stack = seat.stackAmount

    if (stack >= max) {
      val e = new err.BuyIn.Stack.EnoughToPlay(player, stack, max)
      events.one(player).publish(e)
      return
    }

    val f = balance.availableWithBonus(player, min.toDouble)
    f.onSuccess { amount =>
      if (amount + stack < min) {
        val e = new err.BuyIn.Balance.NotEnoughToRebuy(player, amount, stack, min)
        events.one(player).publish(e)
      } else {
        events.one(player).publish(gameplay.Events.requireRebuy(seat, stake, amount))
      }
    }
  }

  protected def buyInSeat(seat: Sitting, amount: Decimal) {
    val player = seat.player

    val (min, max) = stake.buyInAmount
    if (min > amount) {

    }
    if (min < amount) {

    }

    val f = balance.join(roomId, player, amount.toDouble)
    f.onSuccess { _ =>
      // TODO: reserve seat first
      seat.buyIn(amount)
      events broadcast gameplay.Events.playerJoin(seat)
    }

    f.onFailure {
      case err: de.pokerno.payment.thrift.NotEnoughMoney =>
        log.error("balance error: {}", err.message)
        events.one(player).publish(gameplay.Events.error(err))

      case err: de.pokerno.payment.thrift.BuyInRequired =>
        log.error("balance error: {}", err.message)
        events.one(player).publish(gameplay.Events.error(err))
    }
  }

  protected def rebuySeat(seat: Sitting, amount: Decimal) {
    val player = seat.player

    if (seat.rebuy.isDefined) {
      events.one(player).publish(gameplay.Events.notice("You have already made rebuy. Rebuy amount will be affected after current deal finishes."))
      return
    }

    val (requiredMin, requiredMax) = stake.buyInAmount
    val stack = seat.stackAmount

    if (stack >= requiredMax) {
      events.one(player).publish(gameplay.Events.error("You have enough money to play at this table."))
      return
    }

    val f = balance.buyin(roomId, player, amount.toDouble)
    f.onSuccess { _ =>
      seat.rebuy(amount)
      if (seat.rebuy.isDefined) {
        events.one(player).publish(gameplay.Events.notice("Rebuy amount will be affected after current deal finishes."))
      } else {
        events.broadcast(gameplay.Events.stackChange(seat))
      }
    }

    f.onFailure {
      case err: de.pokerno.payment.thrift.NotEnoughMoney =>
        log.error("balance error: {}", err.message)
        events.one(player).publish(gameplay.Events.error(err))

      case err: de.pokerno.payment.thrift.BuyInRequired =>
        log.error("balance error: {}", err.message)
        events.one(player).publish(gameplay.Events.error(err))
    }
  }

  def leaveSeat(seat: Sitting, kick: Boolean = false) {
    table.clear(seat.pos)
    if (seat.buyInAmount > 0) {
      balance.leave(roomId, seat.player, seat.buyInAmount.toDouble)
    }
    events broadcast gameplay.Events.playerLeave(seat, kick)
  }

  def leavePlayer(player: Player, kick: Boolean = false) {
    table(player) map { seat =>
      if (seat.notActive || notRunning) {
        leaveSeat(seat, kick)
      } else {
        seat.leaving
        running map { case Room.Running(ctx, ref) =>
          ref ! gameplay.Betting.Cancel(player)
        }
      }
    }
  }

}
