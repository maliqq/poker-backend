package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.ActorRef

/*
 * Стадия принудительных ставок-анте
 */
private[gameplay] trait Antes {

  betting: Betting ⇒

  def postAntes(ctx: StageContext) = {
    val gameOptions = ctx.gameplay.game.options
    val stake = ctx.gameplay.stake
    val round = ctx.gameplay.round

    if (gameOptions.hasAnte || stake.ante.isDefined) {
      val seats = ctx.gameplay.round.seats filter (_._1 isActive)

      seats foreach (forceBet(ctx, _, Bet.Ante))

      ctx.gameplay.completeBetting(ctx)
    }
  }

}
