package de.pokerno.replay

import de.pokerno.gameplay.{ Events, Stage, stg }
import de.pokerno.model.{Player, DealType}
import de.pokerno.poker.{Cards, Deck}
import de.pokerno.protocol.cmd

private[replay] case class Dealing(
    ctx: Context,
    _type: DealType.Value,
    dealActions: Seq[cmd.DealCards]
  ) extends Stage {
  
  import ctx._
  import ctx.gameplay._
  
  private val _perPlayer = collection.mutable.HashMap[Player, cmd.DealCards]()
  dealActions.foreach { action ⇒
    action.player map { p =>
      _perPlayer(p) = action
    }
  }
  
  def apply() = {
    table.seats.zipWithIndex filter (_._1 isActive) foreach {
      case (seat, pos) ⇒
        val player = seat.player.get
        if (_perPlayer.contains(player)) {
          val d = _perPlayer(player)
          dealCards(d._type, d.cards, Some(player))
        } else {
          val pocketSize = gameOptions.pocketSize
          dealCards(_type,
              cards = Right(Some(pocketSize)),
              player = Some(player)
              )
        }
        sleep()
    }
  }

  private def dealCards(_type: DealType.Value, cards: Either[Cards, Option[Int]], player: Option[Player]) = _type match {
    case DealType.Hole | DealType.Door ⇒

      val (_player: Player, pos: Int) = player match {
        case Some(p) ⇒
          (p, table.playerPos(p))

        case None ⇒
          val pos = round.current
          val seat = table.seats(pos)
          (seat.player.get, pos)
      }
      
      val pocketSize = gameOptions.pocketSize

      def dealPocket(cards: Either[Cards, Int], player: Player): Cards = {
        val max = pocketSize - dealer.pocketOption(_player).map(_.size).getOrElse(0)

        cards match {
          case Right(n) if n > 0 ⇒
            dealer.dealPocket(List(n, max).min, _player)

          case Left(cards) if !cards.isEmpty ⇒
            dealer.dealPocket(cards.take(max), _player)

          case _ ⇒ null
        }
      }

      val cardsDealt: Cards = cards match {
        case Left(cards) =>     dealPocket(Left(cards), _player)
        case Right(Some(n)) =>  dealPocket(Right(n), _player)
        case Right(None) =>     dealPocket(Right(pocketSize), _player)
      }

      ctx broadcast Events.dealPocket(pos, _player, _type, cardsDealt)

    case DealType.Board if gameOptions.hasBoard ⇒

      def dealBoard(cards: Either[Cards, Int]): Cards = {
        val max = Deck.FullBoardSize - dealer.board.size

        cards match {
          case Right(n) if n > 0 ⇒
            dealer.dealBoard(List(n, max).min)

          case Left(cards) if !cards.isEmpty ⇒
            dealer.dealBoard(cards.take(max))

          case _ ⇒ null
        }
      }

      val cardsDealt: Cards = cards match {
        case Left(cards) =>     dealBoard(Left(cards))
        case Right(Some(n)) =>  dealBoard(Right(n))
        case Right(None) =>     null
      }

      ctx broadcast Events.dealBoard(cardsDealt)
  }
}
