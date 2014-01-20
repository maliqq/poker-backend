package de.pokerno.gameplay

import de.pokerno.protocol.{msg, rpc}
import de.pokerno.model._
import akka.actor.{Actor, ActorRef, ActorLogging}

object Replay {
  case class Subscribe(out: ActorRef)
}

class Replay(val variation: Variation, val stake: Stake) extends Actor with ActorLogging {
  
  import context._
  
  val table = new Table(variation.tableSize)
  
  lazy val events = new GameplayEvents
  lazy val dealer = new Dealer
  
  val betting: ActorRef = system.deadLetters
  
  override def preStart {
    Console printf("starting replay with variation=%s stake=%s", variation, stake)
  }
  
  def receive = {
    case Replay.Subscribe(out) =>
      events.broker.subscribe(out, "replay-out")
      
    case rpc.JoinPlayer(pos, player, amount) =>
      table.addPlayer(pos, player, Some(amount))
      events.joinTable((player, pos), amount)
      
    case rpc.AddBet(player, bet) =>
      events.addBet(table.box(player), bet)
      
    case rpc.ShowCards(cards, player, muck) =>
      events.showCards(table.box(player), cards, muck)
      
    case rpc.DealCards(_type, cards, player, cardsNum) =>
      _type match {
        case DealCards.Hole =>
          Console printf(" | deal %s -> %s\n", cards, player)
          dealer.dealPocket(cards, player)
          events.dealCards(_type, cards, Some(table.box(player)))
        
        case DealCards.Door =>
          Console printf(" | deal door %s -> %s\n", cards, player)
          dealer.dealPocket(cards, player)
          events.dealCards(_type, cards, Some(table.box(player)))
          
        case DealCards.Board =>
          Console printf(" | deal board %s\n", cards)
          dealer.dealBoard(cards)
          events.dealCards(_type, cards)
      }
  }
  
}
