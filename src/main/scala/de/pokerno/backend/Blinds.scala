package de.pokerno.backend

import de.pokerno.model._

import akka.actor.ActorRef

trait Blinds {
  g: GameplayLike ⇒
  def postBlinds(betting: ActorRef) = if (game.options.hasBlinds) {
    moveButton

    val seats = round seats
    val active = seats where (_ isActive)
    val waiting = seats where (_ isWaitingBB)

    if (active.size + waiting.size < 2) {
      //
    } else {
      val List(sb, bb, _*) = active

      round.forceBet(sb, Bet.SmallBlind)
      round.forceBet(bb, Bet.BigBlind)
    }
  }
  
  def moveButton {
    table.button.move
    round.current = table.button
    events.publish(
        protocol.Message.ButtonChange(button = table.button)
      )
  }
}
