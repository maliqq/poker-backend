package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

class PostAntes(val betting: Betting.Context) {
  def run(context: Gameplay.Context) {
    betting.foreach(context.table.active) { case (seat, pos) =>
      val bet = Bet.force(Bet.Ante, context.stake)
      betting.force(bet)
      context.broadcast.all(Message.AddBet(Bet.Ante, pos = Some(pos), bet = bet))
    }
  }
}
