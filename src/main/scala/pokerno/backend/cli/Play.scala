package pokerno.backend.cli

import pokerno.backend.model._
import pokerno.backend.poker._
import pokerno.backend.engine._
import pokerno.backend.protocol._
import akka.actor.{ Actor, ActorRef }
import scala.math.{ BigDecimal ⇒ Decimal }
import java.util.Scanner

class Play(gameplay: Gameplay, instance: ActorRef) extends Actor {
  override def preStart = {
    Console println("starting play")
  }
  
  def receive = {
    case join: Play.Join =>
      val stack = 1500.0
      (1 to join.tableSize) foreach { i =>
        val player = new Player("player-%d".format(i))
        join.deal ! Message.JoinTable(pos = i - 1, player = player, amount = stack)
      }
      
    case msg: Message.RequireBet =>
      Console printf("call=%.2f raise=%.2f..%.2f\n", msg.call, msg.min, msg.max)
      
      val seat = gameplay.table.seats(msg.pos)
      Console printf("SEAT=%s", seat)
      var bet: Option[Bet] = None
      while (bet.isEmpty) {
        bet = Some(Play.readBet(msg.call, msg.call - seat.put.getOrElse(.0)))
      }
      instance ! Message.AddBet(pos = msg.pos, bet = bet.get)
      
    case msg: Message.RequireDiscard =>
      val seat = gameplay.table.seats(msg.pos)

      Console printf("your cards: [%s]\n", gameplay.dealer.pocket(seat.player.get))
      
      val cards = Play.readCards
      
      instance ! Message.DiscardCards(pos = msg.pos, cards = cards)

    case msg: Message.DealCards => msg._type match {
      case Dealer.Board =>
        val cards: Cards = msg.cards
        Console printf("Dealt %s %s\n", msg._type, cards toConsoleString)
      case _ =>
        val cards: Cards = msg.cards
        Console printf("Dealt %s %s to %d\n", msg._type, cards toConsoleString, msg.pos.get)
    }
    
    case msg: Message.MoveButton =>
      Console printf("Button is %d\n", msg.pos + 1)

    case msg: Message.AddBet =>
      val seat = gameplay.table.seats(msg.pos)

      Console printf("Player %s: %s\n", seat.player.get, msg.bet)

    case msg: Message.CollectPot =>
      Console printf("Pot size: %.2f\nBoard: %s\n", msg.total, gameplay.dealer.board)

    case msg: Message.ShowHand =>
      val seat = gameplay.table.seats(msg.pos)
      
      Console printf("Player %s has %s (%s)\n", seat.player.get, msg.cards, msg.hand)

    case msg: Message.Winner =>
      val seat = gameplay.table.seats(msg.pos)
      
      Console printf("Player %s won %.2f\n", seat.player.get, msg.amount)
  }
}

object Play {
  case class Join(tableSize: Int, deal: ActorRef)
  
  val sc = new Scanner(System.in)

  def readBet(call: Decimal, toCall: Decimal): Bet = {
    var bet: Option[Bet] = None
    while (bet.isEmpty) {
      Console print (">>> ")
      bet = parseBet(call, toCall, sc.nextLine)
    }
    bet.get
  }

  def parseBet(call: Decimal, toCall: Decimal, str: String): Option[Bet] = str match {
    case "" ⇒
      if (toCall == .0) Some(Bet.check)
      else Some(Bet.call(toCall))
    case "fold"  ⇒ Some(Bet.fold)
    case "check" ⇒ Some(Bet.check)
    case "call"  ⇒ Some(Bet.call(toCall))
    case _ ⇒
      val parts = str.split(" ")
      val amountStr: String = if (parts.size == 1) parts.head
      else if (parts.size == 2 && parts.head == "raise") parts.last
      else ""
      if (amountStr == "") None
      else {
        try {
          val amount = Decimal(amountStr)
          Some(Bet.raise(amount))
        } catch {
          case _: java.lang.NumberFormatException ⇒ None
        }
      }
  }

  def readCards: List[Card] = {
    var cards: Option[List[Card]] = None

    while (cards.isEmpty) {
      val str = sc.nextLine
      try {
        cards = Some(Cards(str))
      } catch {
        case _: Card.ParseError ⇒ cards = None
      }
    }
    
    cards.get
  }
}
