package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

class PostAntes(val context: Gameplay.Context) {
  def run {
    context.betting.foreach(context.table.active) { case (seat, pos) =>
      val bet = Bet.force(Bet.Ante, context.stake)
      context.betting.force(bet)
      context.broadcast.all(Message.AddBet(Bet.Ante, pos = Some(pos), bet = bet))
    }
  }
}
