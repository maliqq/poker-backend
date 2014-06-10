package de.pokerno.gameplay.stages

import de.pokerno.model._
import de.pokerno.gameplay.{Events, Stage, stg}

case class Dealing(ctx: stg.Context, _type: DealType.Value, cardsNum: Option[Int] = None) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = _type match {
    case DealType.Hole | DealType.Door ⇒
      var n: Int = cardsNum getOrElse (0)
      if (n <= 0 || n > gameOptions.pocketSize) n = game.options.pocketSize

      table.seats filter (_.isActive) foreach { seat =>
        val player = seat.player.get
        val cards = dealer dealPocket (n, player)
        
        assert(cards.size == n)

        if (_type == DealType.Hole) {
          seat.hole(cards)
          events.publish(Events.dealPocket(seat, _type, cards)) { _.only(player) }
          events.publish(Events.dealPocketNum(seat, _type, cards.size)) { _.except(player) }
        } else {
          seat.door(cards)
          events broadcast Events.dealPocket(seat, _type, cards)
        }
      }

    case DealType.Board if cardsNum.isDefined ⇒

      val cards = dealer dealBoard (cardsNum.get)
    
      assert(cards.size == cardsNum.get)
    
      events broadcast Events.dealBoard(cards)

    case _ ⇒
    // TODO
  }

}
