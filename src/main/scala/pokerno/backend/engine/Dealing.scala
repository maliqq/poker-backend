package pokerno.backend.engine

import pokerno.backend.protocol._
import pokerno.backend.model._

class Dealing(private var _dealType: Dealer.Value) extends Stage {
  def run(context: Gameplay.Context) {
    _dealType match {
    case Dealer.Hole | Dealer.Door =>
      var n: Int = _dealType.cardsNum.getOrElse(0)
      if (n == 0)
        n = context.game.options.pocketSize
      
      val ring = context.table.ring
      ring.active foreach { case (seat, pos) =>
        val message = new Message.DealCards(
            _type = _dealType,
            pos = Some(pos),
            cards = context.dealer.dealPocket(_dealType, n, seat.player.get)
        )
        context.broadcast.all(message)
      }
      
    case Dealer.Board =>
      val message = new Message.DealCards(
          _type = _dealType,
          pos = None,
          cards = context.dealer.dealBoard(_dealType.cardsNum.get)
      )
      context.broadcast.all(message)
    }
  }
}
