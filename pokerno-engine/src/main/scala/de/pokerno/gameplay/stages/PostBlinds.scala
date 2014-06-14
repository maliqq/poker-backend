package de.pokerno.gameplay.stages

import de.pokerno.model._
import de.pokerno.gameplay.{Betting, Stage, stg}
import de.pokerno.util.Colored._

/*
 * Стадия принудительных ставок-блайндов
 */
case class PostBlinds(ctx: stg.Context) extends Stage with Betting {
  
  val gameplay = ctx.gameplay
  import ctx.gameplay._
  
  override def round = bettingRound
  
  def apply() = if (gameOptions.hasBlinds) {
    moveButton() // FIXME
    
    val active      = round.seats filter (_ isActive)
    val waitingBB   = round.seats filter (_ isWaitingBB)
    
    if (active.size + waitingBB.size < 2) {
      // TODO
    } else {
      val Seq(sb, bb, _*) = if (active.size == 2) active.reverse else active

      forceBet(sb, BetType.SmallBlind)
      
      forceBet(bb, BetType.BigBlind)
    }
  }
  
}
