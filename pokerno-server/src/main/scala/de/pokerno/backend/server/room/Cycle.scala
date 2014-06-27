package de.pokerno.backend.server.room

import akka.actor.Props
import concurrent.duration._
import de.pokerno.backend.server.Room
import de.pokerno.model._
import de.pokerno.gameplay
import de.pokerno.gameplay.Events

trait Cycle { room: Room ⇒

  import context._
  import Room._

  final val minimumReadyPlayersToStart = 2
  // TODO move to config
  final val firstDealAfter = 10.seconds
  final val nextDealAfter = 5.seconds

  def tryDealStart(): State = {
    table.sitting.foreach { seat =>
      if (seat.willLeave) {
        leaveSeat(seat)
      } else if (seat.isAllIn) {
        seat.taken()
        balance.available(seat.player).onSuccess { amount =>
          events broadcast Events.requireBuyIn(seat, stake, amount)
        }
      }
      // FIXME: clear cards?
    }
    // TODO to method
    if (canStart) {
      log.info("deal start")
      stay() using startDeal()
    } else {
      log.info("deal cancelled")
      toWaiting() using NoneRunning
    }
  }
  
  protected def canStart: Boolean = {
    table.sitting.count(_ canPlay) >= minimumReadyPlayersToStart
  }
  
  protected def startDeal(): Running = {
    val ctx = new gameplay.Context(roomId, table, variation, stake, balance, events)
    val deal = actorOf(Props(classOf[gameplay.Deal], ctx), name = f"room-$roomId-deal-${ctx.play.id}")
    Running(ctx, deal)
  }

}
