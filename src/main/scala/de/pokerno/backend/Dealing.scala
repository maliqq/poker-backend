package de.pokerno.backend

import de.pokerno.model._
import de.pokerno.backend.{protocol => message}

trait Dealing {
  g: GameplayLike ⇒
  def dealCards(_type: Dealer.DealType, cardsNum: Option[Int] = None) {
    _type match {
      case Dealer.Hole | Dealer.Door ⇒
        var n: Int = cardsNum getOrElse (0)
        if (n == 0)
          n = game.options.pocketSize

        Console printf ("dealing %s %d cards\n", _type, n)

        table.seats where (_ isActive) foreach {
          case (seat, pos) ⇒
            val cards = dealer dealPocket (n, seat.player.get)  
            
            if (_type.isPrivate) {
              events.publish(
                  message.DealCards(_type, cards, pos = pos),
                events.One(seat.player.get.id))
              events.publish(
                  message.DealCards(_type, pos = pos, cardsNum = Some(n))
                )
            } else events.publish(message.DealCards(_type, cards, pos = pos))
        }

      case Dealer.Board ⇒

        Console printf ("dealing board %d cards\n", cardsNum.get)

        val cards = dealer dealBoard (cardsNum.get)
        events.publish(
            message.DealCards(_type, cards)
          )
    }
  }
}
