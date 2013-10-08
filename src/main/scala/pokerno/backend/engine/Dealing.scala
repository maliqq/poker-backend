package pokerno.backend.engine

import pokerno.backend.protocol._
import pokerno.backend.model._

trait Dealing {
  g: Gameplay ⇒
  def dealCards(_dealType: Dealer.DealType, cardsNum: Option[Int] = None) {
    _dealType match {
      case Dealer.Hole | Dealer.Door ⇒
        var n: Int = cardsNum getOrElse (0)
        if (n == 0)
          n = game.options.pocketSize

        Console printf ("dealing %s %d cards\n", _dealType, n)

        table.seats where (_ isActive) foreach {
          case (seat, pos) ⇒
            val message = Message.DealCards(
              _type = _dealType,
              pos = Some(pos),
              cards = dealer dealPocket (_dealType, n, seat.player.get))
            
            if (_dealType.isPrivate) broadcast.one(seat.player.get) { message }
            else broadcast all (message)
        }

      case Dealer.Board ⇒

        Console printf ("dealing board %d cards\n", cardsNum.get)

        broadcast all (Message.DealCards(
          _type = _dealType,
          pos = None,
          cards = dealer dealBoard (cardsNum.get)))
    }
  }
}
