package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.poker.{Card, Deck}
import de.pokerno.protocol.{msg => message}

object Dealing {
  
  trait DealContext {
    
    g: ContextLike ⇒
    
    def dealCards(_type: DealCards.Value, cardsNum: Option[Int] = None) {
      _type match {
        case DealCards.Hole | DealCards.Door ⇒
          var n: Int = cardsNum getOrElse (0)
          if (n <= 0 || n > game.options.pocketSize) n = game.options.pocketSize
  
          Console printf ("dealing %s %d cards\n", _type, n)
  
          (table.seats: List[Seat]).zipWithIndex filter (_._1 isActive) foreach {
            case (seat, pos) ⇒
              val cards = dealer dealPocket (n, seat.player.get)  
              events.dealCards(_type, cards, Some((seat.player.get, pos)))
          }
  
        case DealCards.Board if cardsNum.isDefined ⇒
  
          Console printf ("dealing board %d cards\n", cardsNum.get)
  
          val cards = dealer dealBoard (cardsNum.get)
          events.dealCards(_type, cards)
        
        case _ =>
          // TODO
      }
    }
  }
  
  trait ReplayContext {
    replay: Replay =>
      
    def dealCards(
        _type: DealCards.Value,
        player: Option[Player] = None,
        cards: Option[List[Card]] = None,
        cardsNum: Option[Int] = None) {
      
      val gameOptions = gameplay.game.options
      val dealer = gameplay.dealer
      
      (_type: DealCards.Value) match {
        case DealCards.Hole | DealCards.Door =>
          
          val (_player: Player, pos: Int) =
            if (player.isDefined)
              t.box(player.get)
            else {
              val (seat, pos) = gameplay.round.acting
              (seat.player.get, pos)
            }
          
          val pocketSize = gameOptions.pocketSize
          
          def dealPocket(cards: Either[Int, List[Card]], player: Player): List[Card] = {
            val cardsNum = cards match {
              case Left(n) => n
              case Right(cards) => cards.size
            }
            if (cardsNum <= 0 || cardsNum > pocketSize)
              cards match {
                case Left(n) =>
                  dealer.dealPocket(pocketSize, _player)
                case Right(cards) =>
                  dealer.dealPocket(cards.take(pocketSize), _player)
              }
            else List.empty
          }
          
          val cardsDealt: List[Card] = if (cards.isDefined && !cards.get.isEmpty)
            dealPocket(Right(cards.get), _player)
          else
            dealPocket(Left(cardsNum.getOrElse(pocketSize)), _player)
          
          log.debug(" | deal {} -> {}", cardsDealt, _player)
          e.dealCards(_type, cardsDealt, Some(_player, pos))
    
        case DealCards.Board if gameOptions.hasBoard =>
          
          def dealBoard(cards: Either[Int, List[Card]]): List[Card] = {
            val cardsNum = cards match {
              case Left(n) => n
              case Right(cards) => cards.size
            }
            if (cardsNum > 0 && dealer.board.size + cardsNum <= Deck.FullBoardSize)
              cards match {
                case Left(n) => dealer.dealBoard(n)
                case Right(cards) => dealer.dealBoard(cards)
              }
            else List.empty
          }
          
          val cardsDealt: List[Card] = if (cards.isDefined && !cards.get.isEmpty)
            dealBoard(Right(cards.get))
          else
            dealBoard(Left(cardsNum.getOrElse(0)))

          log.debug(" | deal board {}", cardsDealt)
          e.dealCards(_type, cardsDealt)
          
      }
    }
  }

}