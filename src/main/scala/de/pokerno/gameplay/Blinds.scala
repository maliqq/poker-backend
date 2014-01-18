package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.{msg => message}

import akka.actor.ActorRef

trait Blinds {
  g: GameplayLike ⇒
  def postBlinds(betting: ActorRef) = if (game.options.hasBlinds) {
    moveButton

    val seats = round seats
    val active = seats filter (_._1 isActive)
    val waiting = seats filter (_._1 isWaitingBB)

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
        message.ButtonChange(_button = table.button)
      )
  }
}
