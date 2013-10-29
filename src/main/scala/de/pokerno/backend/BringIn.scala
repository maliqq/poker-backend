package de.pokerno.backend

import de.pokerno.backend.{protocol => message}

import akka.actor.ActorRef

trait BringIn {
  g: GameplayLike ⇒

  def bringIn(betting: ActorRef) {
    val (seat, pos) = round.seats where (_ isActive) minBy {
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
    events.publish(message.ButtonChange(button = table.button))
  }

}
