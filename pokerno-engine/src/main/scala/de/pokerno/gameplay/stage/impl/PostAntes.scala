package de.pokerno.gameplay.stage.impl

import de.pokerno.model._
import de.pokerno.gameplay.{Betting, Stage}

/*
 * Стадия принудительных ставок-анте
 */

private[gameplay] case class PostAntes(ctx: Stage.Context) extends Stage with Betting {
  
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
