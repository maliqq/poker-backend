package de.pokerno.gameplay.stages

import de.pokerno.model._
import de.pokerno.gameplay.{Events, Stage, stg}

case class Dealing(ctx: stg.Context, _type: DealType.Value, cardsNum: Option[Int] = None) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = _type match {
    case DealType.Hole | DealType.Door ⇒
      var n: Int = cardsNum getOrElse (0)
      if (n <= 0 || n > gameOptions.pocketSize) n = game.options.pocketSize

      Console printf("dealing %s %d cards\n", _type, n)

      table.seats filter (_.isActive) foreach { seat =>
        val player = seat.player.get
        val cards = dealer dealPocket (n, player)

        if (_type == DealType.Hole) {
          events.publish(Events.dealPocket(seat, _type, cards)) { _.only(player) }
          events.publish(Events.dealPocketNum(seat, _type, cards.length)) { _.except(player) }
        } else events broadcast Events.dealPocket(seat, _type, cards)
      }

    case DealType.Board if cardsNum.isDefined ⇒

      Console printf("dealing board %d cards\n", cardsNum.get)

      val cards = dealer dealBoard (cardsNum.get)
      events broadcast Events.dealBoard(cards)

    case _ ⇒
    // TODO
  }

}
