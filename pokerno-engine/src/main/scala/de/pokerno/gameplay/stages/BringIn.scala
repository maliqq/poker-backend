package de.pokerno.gameplay.stages

import de.pokerno.gameplay.{Stage, stg}

/*
 * Стадия принудительных ставок - бринг-ин
 */
case class BringIn(ctx: stg.Context) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = {
    val seat = round.seats filter (_.isActive) minBy { _seat ⇒
      dealer.pocket(_seat.player.get).last
    }
    
    setButton(seat.pos)
    
    // FIXME wtf?
    //ctx.ref ! Betting.Require(stake.bringIn get, game.limit)
  }
  
}
