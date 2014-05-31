package de.pokerno.gameplay

import de.pokerno.model._

/*
 * Стадия принудительных ставок-анте
 */

private[gameplay] case class PostAntes(ctx: StageContext) extends Stage(ctx) with Betting {
  
  def process =

    if (game.options.hasAnte || stake.ante.isDefined) {
      round.seats filter (_._1.isActive) foreach { case (_, pos) =>
        forceBet(pos, Bet.Ante)
      }
  
      completeBetting()
    }
  
}
