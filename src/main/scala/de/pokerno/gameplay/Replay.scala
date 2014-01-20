package de.pokerno.gameplay

import de.pokerno.protocol.{msg, rpc}
import de.pokerno.model._
import akka.actor.{Actor, Props, ActorRef, ActorLogging}

object Replay {
  case class Subscribe(out: ActorRef)
}

class Replay(val variation: Variation, val stake: Stake) extends Actor with ActorLogging {
  
  import context._
  
  val table = new Table(variation.tableSize)
  val gameplay = new Gameplay(events, variation, stake, table)
  
  lazy val events = new GameplayEvents
  lazy val dealer = new Dealer
  
  var betting: ActorRef = system.deadLetters

  override def preStart {
    Console printf("starting replay with variation=%s stake=%s\n", variation, stake)
    
    betting = actorOf(Props(classOf[BettingActor], gameplay.round), name = "betting-process")
  }
  
  def receive = {
    case Replay.Subscribe(out) =>
      events.broker.subscribe(out, "replay-out")
      events.start(table, variation, stake)

    case rpc.JoinPlayer(pos, player, amount) =>
      table.addPlayer(pos, player, Some(amount))
      events.joinTable((player, pos), amount)
      
    case addBet @ rpc.AddBet(player, bet) =>
      if (bet.isForced) {
        
        val (_, pos) = table.box(player)
        val seat = (table.seats: List[Seat])(pos)
        
        gameplay.round.forceBet((seat, pos), bet.betType.asInstanceOf[Bet.ForcedBet])

      } else {
        betting ! addBet
        events.addBet(table.box(player), bet)
      }
      
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
    
    case Street.Start =>
      gameplay.prepareSeats
      self ! Street.Next

    case Street.Next =>
      log.info("next street")
      betting ! Betting.Next

    case Street.Exit =>
      log.info("showdown")
      gameplay.showdown
      stop(self)
  }
  
}
