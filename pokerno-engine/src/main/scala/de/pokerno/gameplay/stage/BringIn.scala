package de.pokerno.gameplay

import de.pokerno.protocol.{ msg ⇒ message }

import akka.actor.ActorRef

/*
 * Стадия принудительных ставок - бринг-ин
 */
private[gameplay] case class BringIn(ctx: StageContext) extends Stage(ctx) {
  
  def process() = {
    val (_, pos) = round.seats filter (_._1.isActive) minBy { case (_seat, _pos) ⇒
      dealer.pocket(_seat.player.get).last
    }
    
    gameplay.setButton(pos)
    
    // FIXME wtf?
    //ctx.ref ! Betting.Require(stake.bringIn get, game.limit)
  }
  
}
