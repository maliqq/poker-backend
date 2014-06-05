package de.pokerno.replay

import de.pokerno.gameplay.{ Events, Stage, stg }
import de.pokerno.model.{Player, DealType}
import de.pokerno.poker.{Cards, Deck}
import de.pokerno.protocol.cmd

private[replay] case class Dealing(
    ctx: Context,
    _type: DealType.Value,
    _cardsNum: Option[Int],
    actions: Seq[cmd.Command]
  ) extends Stage {
  
  import ctx._
  import ctx.gameplay._
  
  private val dealActions = actions.filter { action ⇒
    action match {
      case a: cmd.DealCards ⇒   a._type == _type
      case _ ⇒                  false
    }
  }.asInstanceOf[List[cmd.DealCards]]

  Console printf("\nDEALCARD: %s\n\n",dealActions)

  private val _perPlayer = collection.mutable.HashMap[Player, cmd.DealCards]()
  dealActions.foreach { action ⇒
    action.player map { p =>
      _perPlayer(p) = action
    }
  }
  
  def apply() = _type match {
    case DealType.Board if gameOptions.hasBoard =>
      
      if (dealActions.headOption.isDefined) {
        dealBoard(dealActions.head.cards)
      } else {
        dealBoard(Right(_cardsNum))
      }

    case DealType.Door | DealType.Hole =>
      
      table.seats.zipWithIndex filter (_._1 isActive) foreach { case (seat, pos) ⇒
        val player = seat.player.get
        if (_perPlayer.contains(player)) {
          val d = _perPlayer(player)
          dealPocket(d._type, d.cards, Some(player))
        } else {
          dealPocket(_type, Right(None), Some(player))
        }
        sleep()
      }
      
    case _ =>
  }

  private def dealPocket(_type: DealType.Value, _cards: Either[Cards, Option[Int]], _player: Option[Player]) {
    val (player: Player, pos: Int) = _player match {
      case Some(p) ⇒
        (p, table.playerPos(p).get)

      case None ⇒
        val pos = round.current
        val seat = table.seats(pos)
        (seat.player.get, pos)
    }
    
    val pocketSize = gameOptions.pocketSize
    val max = pocketSize - dealer.pocketOption(player).map(_.size).getOrElse(0)
    val cardsDealt = _cards match {
      case Right(n) ⇒
        dealer.dealPocket(List(n.getOrElse(pocketSize), max).min, player)

      case Left(cards) ⇒
        dealer.dealPocket(cards.take(max), player)

      case _ ⇒ null
    }
    
    ctx broadcast Events.dealPocket(pos, player, _type, cardsDealt)
  }
  
  private def dealBoard(_cards: Either[Cards, Option[Int]]) {
    val max = Deck.FullBoardSize - dealer.board.size

    val cards = _cards match {
      case Right(n) ⇒
        dealer.dealBoard(List(n.getOrElse(_cardsNum.get), max).min)

      case Left(cards) if !cards.isEmpty ⇒
        dealer.dealBoard(cards.take(max))

      case _ ⇒ null
    }
    
    ctx broadcast Events.dealBoard(cards)
  }
  
}