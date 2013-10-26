package de.pokerno.backend.engine

import de.pokerno.backend.protocol._
import de.pokerno.backend.model._

trait Dealing {
  g: GameplayLike ⇒
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

            if (_dealType.isPrivate) events.publish(message, events.One(seat.player.get.id))
            else events.publish(message)
        }

      case Dealer.Board ⇒

        Console printf ("dealing board %d cards\n", cardsNum.get)

        events.publish(Message.DealCards(
          _type = _dealType,
          pos = None,
          cards = dealer dealBoard (cardsNum.get)))
    }
  }
}
