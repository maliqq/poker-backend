package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.{msg => message}

trait Dealing {
  
  g: GameplayLike ⇒
  
  def dealCards(_type: DealCards.Value, cardsNum: Option[Int] = None) {
    _type match {
      case DealCards.Hole | DealCards.Door ⇒
        var n: Int = cardsNum getOrElse (0)
        if (n == 0)
          n = game.options.pocketSize

        Console printf ("dealing %s %d cards\n", _type, n)

        (table.seats: List[Seat]).zipWithIndex filter (_._1 isActive) foreach {
          case (seat, pos) ⇒
            val cards = dealer dealPocket (n, seat.player.get)  
            events.dealCards(_type, cards, Some((seat.player.get, pos)))
        }

      case DealCards.Board ⇒

        Console printf ("dealing board %d cards\n", cardsNum.get)

        val cards = dealer dealBoard (cardsNum.get)
        events.dealCards(_type, cards)
    }
  }
}
