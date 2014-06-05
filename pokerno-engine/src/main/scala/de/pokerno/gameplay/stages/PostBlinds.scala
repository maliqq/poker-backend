package de.pokerno.gameplay.stages

import de.pokerno.model._
import de.pokerno.gameplay.{Bets, Stage, stg}

/*
 * Стадия принудительных ставок-блайндов
 */
case class PostBlinds(ctx: stg.Context) extends Stage with Bets {
  
  import ctx.gameplay._
  
  def apply() = if (gameOptions.hasBlinds) {
    moveButton() // FIXME
    
    val active      = round.seats filter (_._1 isActive)      map (_._2)
    val waitingBB   = round.seats filter (_._1 isWaitingBB)   map (_._2)

    if (active.size + waitingBB.size < 2) {
      // TODO
    } else {
      val Seq(sb, bb, _*) = if (active.size == 2) active.reverse else active

      forceBet(sb, Bet.SmallBlind)
      
      forceBet(bb, Bet.BigBlind)
    }
  }
  
}