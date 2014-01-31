package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.ActorRef

/*
 * Стадия принудительных ставок-анте
 */
trait Antes {
  
  def postAntes(ctx: StageContext) = {
    val gameOptions = ctx.gameplay.game.options
    val stake = ctx.gameplay.stake
    
    if (gameOptions.hasAnte || stake.ante.isDefined) {
      val seats = ctx.gameplay.round.seats filter (_._1 isActive)
      seats foreach { seat =>
        ctx.gameplay.round.forceBet(seat, Bet.Ante)
      }
      ctx.gameplay.round.complete
    }
  }
  
}
