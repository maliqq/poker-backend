package de.pokerno.gameplay.stage.impl

import de.pokerno.gameplay.Stage

/*
 * Стадия принудительных ставок - бринг-ин
 */
private[gameplay] case class BringIn(ctx: Stage.Context) extends Stage {
  
  import ctx.gameplay._
  import play.dealer
  
  def apply() = {
    val seat = round.seats filter (_.isActive) minBy { _seat ⇒
      dealer.pocket(_seat.player).last
    }
    
    setButton(seat.pos)
    
    // FIXME wtf?
    //ctx.ref ! Betting.Require(stake.bringIn get, game.limit)
  }
  
}
