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
  
        case DealCards.Board ⇒
  
          Console printf ("dealing board %d cards\n", cardsNum.get)
  
          val cards = dealer dealBoard (cardsNum.get)
          events.dealCards(_type, cards)
      }
    }
  }
  
  trait ReplayContext {
    replay: Replay =>
      
    def dealCards(_type: DealCards.Value, player: Player, cards: List[Card], cardsNum: Int) {
      val gameOptions = gameplay.game.options
      val dealer = gameplay.dealer
      
      (_type: DealCards.Value) match {
        case DealCards.Hole | DealCards.Door =>
          
          val (_player: Player, pos: Int) = if (player == null) {
            val (seat, pos) = gameplay.round.acting
            (seat.player.get, pos)
          } else t.box(player)
          
          val cardsDealt: List[Card] = if (cards == null || cards.isEmpty) {
            // check number of cards to deal
            val _cardsNum = if (cardsNum == null ||
                                cardsNum <= 0 ||
                                cardsNum > gameOptions.pocketSize)
              gameOptions.pocketSize
            else
              cardsNum
            
            dealer.dealPocket(_cardsNum, _player)
          } else {
            dealer.dealPocket(cards, _player)
            cards
          }
          
          log.debug(" | deal {} -> {}", cardsDealt, _player)
          e.dealCards(_type, cardsDealt, Some(_player, pos))
    
        case DealCards.Board if gameOptions.hasBoard =>
          val cardsDealt: List[Card] = if (cards == null) {
            
            if (dealer.board.size + cardsNum <= Deck.FullBoardSize)
              dealer.dealBoard(cardsNum)
            else List.empty
            
          } else {
            dealer.dealBoard(cards)
            cards
          }
          
          log.debug(" | deal board {}", cardsDealt)
          e.dealCards(_type, cardsDealt)
          
      }
    }
  }

}