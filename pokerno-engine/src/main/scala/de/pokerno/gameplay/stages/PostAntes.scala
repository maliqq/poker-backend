package de.pokerno.gameplay.stages

import de.pokerno.model._
import de.pokerno.gameplay.{Bets, Stage, stg}

/*
 * Стадия принудительных ставок-анте
 */

case class PostAntes(ctx: stg.Context) extends Stage with Bets {
  
  import ctx.gameplay._
  
  def apply() =

    if (gameOptions.hasAnte || stake.ante.isDefined) {
      round.seats filter (_._1.isActive) foreach { case (_, pos) =>
        forceBet(pos, Bet.Ante)
      }
  
      doneBets()
    }
  
}
