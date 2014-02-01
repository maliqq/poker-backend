package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.poker.{Card, Deck}
import de.pokerno.protocol.{msg => message, rpc}
import de.pokerno.protocol
import protocol.Conversions._
//import protocol.wire.Conversions._
//import protocol.msg.Conversions._

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
      
    def dealing(dealActions: List[rpc.DealCards], dealOptions: DealingOptions) {
      val dealer = gameplay.dealer
      
      val _type = dealOptions.dealType
      
      _type match {
      case DealCards.Board =>
        
        val dealBoard = dealActions.head.asInstanceOf[rpc.DealCards]
        dealCards(_type,
            cards = if (dealBoard.cards != null) Some(dealBoard.cards)
                    else None,
            cardsNum = dealOptions.cardsNum
        )
        
      case DealCards.Door | DealCards.Hole =>
        
        val dealActionsByPlayer = collection.mutable.HashMap[Player, rpc.DealCards]()
        dealActions.foreach { action =>
          dealActionsByPlayer(action.player) = action
        }
        
        t.seatsAsList.zipWithIndex filter (_._1 isActive) foreach {
          case (seat, pos) ⇒
            val player = seat.player.get
            if (dealActionsByPlayer.contains(player)) {
              val dealPocket = dealActionsByPlayer(player)
              
              dealCards(_type,
                  player = Some(player),
                  cards = if (dealPocket.cards != null) Some(dealPocket.cards)
                          else None,
                  cardsNum = if (dealPocket.cardsNum != null) Some(dealPocket.cardsNum)
                             else None
                  )
            } else {
              val pocketSize = gameOptions.pocketSize
              val n = dealOptions.cardsNum.getOrElse(pocketSize)
              val cards = dealer.dealPocket(n, player)
              e.dealCards(_type, cards, Some((seat.player.get, pos)))
            }
        }
      }
    } 
      
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
              t.box(player.get).get
            else {
              val (seat, pos) = gameplay.round.acting
              (seat.player.get, pos)
            }
          
          val pocketSize = gameOptions.pocketSize
          
          def dealPocket(cards: Either[Int, List[Card]], player: Player): List[Card] = {
            val max = pocketSize - dealer.pocketOption(_player).map(_.size).getOrElse(0)
            
            cards match {
              case Left(n) if n > 0 =>
                dealer.dealPocket(List(n, max).min, _player)
              
              case Right(cards) if !cards.isEmpty =>
                dealer.dealPocket(cards.take(max), _player)
              
              case _ => List.empty
            }
          }
          
          val cardsDealt: List[Card] = if (cards.isDefined && !cards.get.isEmpty)
            dealPocket(Right(cards.get), _player)
          else
            dealPocket(Left(cardsNum.getOrElse(pocketSize)), _player)
          
          log.info(" | deal {} -> {}", cardsDealt, _player)
          e.dealCards(_type, cardsDealt, Some(_player, pos))
    
        case DealCards.Board if gameOptions.hasBoard =>
          
          def dealBoard(cards: Either[Int, List[Card]]): List[Card] = {
            val max = Deck.FullBoardSize - dealer.board.size
            
            cards match {
              case Left(n) if n > 0=>
                dealer.dealBoard(List(n, max).min)
                
              case Right(cards) if !cards.isEmpty =>
                dealer.dealBoard(cards.take(max))
              
              case _ => List.empty
            }
          }
          
          val cardsDealt: List[Card] = if (cards.isDefined && !cards.get.isEmpty)
            dealBoard(Right(cards.get))
          else
            dealBoard(Left(cardsNum.getOrElse(0)))

          log.info(" | deal board {}", cardsDealt)
          e.dealCards(_type, cardsDealt)
          
      }
    }
  }

}