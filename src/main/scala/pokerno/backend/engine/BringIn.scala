package pokerno.backend.engine

import pokerno.backend.protocol._
import akka.actor.ActorRef

trait BringIn {
  g: Gameplay ⇒

  def bringIn(betting: ActorRef) {
    val (seat, pos) = round.seats where (_ isActive) minBy {
      case (seat, pos) ⇒
        dealer pocket (seat.player get) last
    }
    setButton(pos)
    round.acting = (seat, pos)

    betting ! Betting.Require(stake.bringIn get, game.limit)
  }
}
