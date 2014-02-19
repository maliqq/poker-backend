package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.{ msg ⇒ message }

import akka.actor.ActorRef

/*
 * Стадия принудительных ставок-блайндов
 */
private[gameplay] trait Blinds {

  betting: Betting ⇒

  def postBlinds(ctx: StageContext) = if (ctx.gameplay.game.options.hasBlinds) {
    ctx.gameplay.moveButton // FIXME
    val round = ctx.gameplay.round

    val seats = round.seats
    val active = seats filter (_._1 isActive)
    val waitingBB = seats filter (_._1 isWaitingBB)

    if (active.size + waitingBB.size < 2) {
      // TODO
    } else {
      val List(sb, bb, _*) = if (active.size == 2){
        active.reverse
      } else active

      forceBet(ctx, sb, Bet.SmallBlind)

      forceBet(ctx, bb, Bet.BigBlind)
    }
  }

}
