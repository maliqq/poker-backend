package de.pokerno.gameplay.stage

import de.pokerno.model._
import de.pokerno.gameplay.{Bets, Stage, StageContext}

/*
 * Стадия принудительных ставок-анте
 */

private[gameplay] case class PostAntes(ctx: StageContext) extends Stage with Bets {
  
  import ctx.gameplay._
  
  def apply() =

    if (game.options.hasAnte || stake.ante.isDefined) {
      round.seats filter (_._1.isActive) foreach { case (_, pos) =>
        forceBet(pos, Bet.Ante)
      }
  
      doneBets()
    }
  
}
