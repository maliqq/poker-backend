package de.pokerno.gameplay

import de.pokerno.protocol.{msg => message}

import akka.actor.ActorRef

trait BringIn {
  g: GameplayLike ⇒

  def bringIn(betting: ActorRef) {
    val (seat, pos) = round.seats filter (_._1 isActive) minBy {
      case (seat, pos) ⇒
        dealer pocket (seat.player get) last
    }
    setButton(pos)
    round.acting = (seat, pos)

    betting ! Betting.Require(stake.bringIn get, game.limit)
  }
  
  private def setButton(pos: Int) {
    table.button.current = pos
    round.current = pos
    events.publish(message.ButtonChange(_button = table.button))
  }

}
