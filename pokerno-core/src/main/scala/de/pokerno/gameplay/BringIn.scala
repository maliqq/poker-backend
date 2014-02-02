package de.pokerno.gameplay

import de.pokerno.protocol.{msg => message}

import akka.actor.ActorRef

/*
 * Стадия принудительных ставок - бринг-ин
 */
trait BringIn {
  
  def bringIn(ctx: StageContext) {
    val (seat, pos) = ctx.gameplay.round.seats filter (_._1 isActive) minBy {
      case (_seat, _pos) ⇒
        ctx.gameplay.dealer.pocket(_seat.player.get).last
    }
    ctx.gameplay.setButton(pos)
    ctx.gameplay.round.acting = (seat, pos)
    // FIXME wtf?
    ctx.ref ! Betting.Require(ctx.gameplay.stake.bringIn get, ctx.gameplay.game.limit)
  }
  
}
