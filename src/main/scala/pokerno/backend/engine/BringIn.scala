package pokerno.backend.engine

import pokerno.backend.protocol._

class BringIn(val context: Gameplay.Context, val betting: Betting.Context) extends Stage {
  def run {
    val ring = context.table.ring
    val (seat, pos) = ring.active.minBy { case (seat, pos) =>
      val pocketCards = context.dealer.pocket(seat.player.get)
      pocketCards.last
    }
    context.setButton(pos)
    betting.start(ring.active)
    val range = betting.raiseRange(context.game.limit, context.stake)
    val (call, min, max) = betting.require(range)
    val message = new Message.RequireBet(call = call, min = min, max = max, pos = pos)
    context.broadcast.all(message)
  }
}
