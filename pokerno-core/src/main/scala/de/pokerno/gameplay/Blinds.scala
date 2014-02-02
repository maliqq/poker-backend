package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.{msg => message}

import akka.actor.ActorRef

/*
 * Стадия принудительных ставок-блайндов
 */
trait Blinds {
  
  t: Betting =>
  
  def postBlinds(ctx: StageContext) = if (ctx.gameplay.game.options.hasBlinds) {
    ctx.gameplay.moveButton
    val round = ctx.gameplay.round

    val seats = round.seats
    val active = seats filter (_._1 isActive)
    val waiting = seats filter (_._1 isWaitingBB)

    if (active.size + waiting.size < 2) {
      //
    } else {
      val List(sb, bb, _*) = active

      forceBet(ctx, sb, Bet.SmallBlind)
      
      forceBet(ctx, bb, Bet.BigBlind)
    }
  }
  
}
