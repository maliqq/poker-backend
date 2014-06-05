package de.pokerno.gameplay.stages

import de.pokerno.gameplay.{Stage, stg}

/*
 * Стадия принудительных ставок - бринг-ин
 */
case class BringIn(ctx: stg.Context) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = {
    val (_, pos) = round.seats filter (_._1.isActive) minBy { case (_seat, _pos) ⇒
      dealer.pocket(_seat.player.get).last
    }
    
    setButton(pos)
    
    // FIXME wtf?
    //ctx.ref ! Betting.Require(stake.bringIn get, game.limit)
  }
  
}