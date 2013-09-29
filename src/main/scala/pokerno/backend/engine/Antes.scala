package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

class PostAntes(val betting: Betting.Context) extends Stage {
  def run(context: Gameplay.Context) {
    betting.foreach(context.table.ring.active) { case (seat, pos) =>
      val bet = Bet.force(Bet.Ante, context.stake)
      betting.force(bet)
      context.broadcast.all(new Message.AddBet(Bet.Ante, pos = Some(pos), bet = bet))
    }
  }
}
