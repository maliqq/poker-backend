package de.pokerno.gameplay.stages

import de.pokerno.model._
import de.pokerno.gameplay.{Betting, Stage, stg}

/*
 * Стадия принудительных ставок-анте
 */

case class PostAntes(ctx: stg.Context) extends Stage with Betting {
  
  val gameplay = ctx.gameplay
  
  import ctx.gameplay._
  
  override def round = bettingRound
  
  def apply() =

    if (gameOptions.hasAnte || stake.ante.isDefined) {
      round.seats filter (_.isActive) foreach { seat =>
        forceBet(seat, BetType.Ante)
      }
  
      complete()
    }
  
}
