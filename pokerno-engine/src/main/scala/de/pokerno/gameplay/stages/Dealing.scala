package de.pokerno.gameplay.stages

import de.pokerno.model._
import de.pokerno.gameplay.{Events, Stage, stg}

private[gameplay] case class Dealing(ctx: stg.Context, _type: DealType.Value, cardsNum: Option[Int] = None) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = _type match {
    case DealType.Hole | DealType.Door ⇒
      val n: Int = {
        val _n = cardsNum getOrElse (0)
        if (_n <= 0 || _n > gameOptions.pocketSize) game.options.pocketSize
        else _n
      }

      table.sitting filter (_.isActive) foreach { seat =>
        val player = seat.player
        val cards = dealer dealPocket (n, player)
        
        assert(cards.size == n)
        assert(cards.forall { _.toByte != 0 })

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
      assert(cards.forall { _.toByte != 0 })
    
      play.board ++= cards.toBuffer
      events broadcast Events.dealBoard(cards)

    case _ ⇒
    // TODO
  }

}
