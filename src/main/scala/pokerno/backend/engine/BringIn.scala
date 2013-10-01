package pokerno.backend.engine

import pokerno.backend.protocol._

class BringIn extends Stage {
  def run(context: Gameplay.Context) {
    val active = context.table.where(_.isActive)
    val (seat, pos) = active.minBy { case (seat, pos) =>
      val pocketCards = context.dealer.pocket(seat.player.get)
      pocketCards.last
    }
    context.setButton(pos)
    context.betting = new Betting.Context(active)
    val range = context.betting.raiseRange(context.game.limit, context.stake)
    val (call, min, max) = context.betting.require(range)
    val message = Message.RequireBet(call = call, min = min, max = max, pos = pos)
    context.broadcast.all(message)
  }
}
