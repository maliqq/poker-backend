package pokerno.backend.engine

import pokerno.backend.protocol._

class BringIn {
  def run(gameplay: Gameplay) {
    val active = gameplay.table where(_ isActive)
    val (seat, pos) = active minBy { case (seat, pos) =>
      val pocketCards = gameplay.dealer pocket(seat.player get)
      pocketCards last
    }
    gameplay.setButton(pos)
    gameplay.betting = new Betting.Context(active)
    val range = gameplay.betting raiseRange(gameplay.game.limit, gameplay.stake)
    val (call, min, max) = gameplay.betting require(range)
    val message = Message.RequireBet(call = call, min = min, max = max, pos = pos)
    gameplay.broadcast all(message)
  }
}
