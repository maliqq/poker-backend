package de.pokerno.cli.play

import jline.console.ConsoleReader
import math.{ BigDecimal ⇒ Decimal }
import concurrent.duration._
import de.pokerno.model._
import de.pokerno.poker._
import de.pokerno.gameplay._
import de.pokerno.protocol._
import msg.Conversions._
import wire.Conversions._
import Conversions._ // FIXME: ProtocolConversions
import akka.actor.{ Actor, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout

object PlayerActor {
  case object Start
}

class PlayerActor(pos: Int, instance: ActorRef) extends Actor {
  val stack = 1500.0
  val player = new Player("player-%d".format(pos))
  var pocketCards: List[Card] = List()

  import context._

  def receive = {
    case PlayerActor.Start ⇒
      instance ! rpc.JoinPlayer(pos = pos, player = player, amount = stack)
      become({
        case msg.DealCards(_type, cards, _pos, _player, cardsNum) ⇒ (_type: DealCards.Value) match {
          case DealCards.Hole | DealCards.Door if _pos == pos =>
            Console printf ("Dealt %s %s to %d\n", _type, Cards(cards).toConsoleString, pos)
            pocketCards = cards
            
          case DealCards.Board =>
            Console printf ("Dealt %s %s\n", _type, Cards(cards).toConsoleString)
          
          case _                         ⇒
        }

        case msg.RequireBet(_pos, _player, call, range) if _pos == pos ⇒
          Console printf ("Seat %d: Call=%.2f Min=%.2f Max=%.2f\n", pos, call, range.min, range.max)

          var bet = Play.readBet(call)

          val addBet = rpc.AddBet(_player, bet)
          
          instance ! addBet

        case msg.RequireDiscard(_pos, _player) if _pos == pos ⇒
          Console printf ("your cards: [%s]\n", pocketCards)

          val cards = Play.readCards

          instance ! msg.DiscardCards(cards)
      })
  }
}

class Play(instance: ActorRef, tableSize: Int) extends Actor {
  import context._
  
  val seats: List[Seat] = List.fill(tableSize) { new Seat }

  override def preStart() {
    Console println "starting play"
    instance ! Instance.Subscribe(self, "play-observer")
    
    (1 to tableSize) foreach { i ⇒
      val playerActor = system.actorOf(Props(classOf[PlayerActor], i - 1, instance), name = "player-process-%d".format(i))
      playerActor ! PlayerActor.Start
    }
  }
  
  var boardCards: List[Card] = List()

  def receive = {
    case msg.DealCards(_type, cards, pos, player, cardsNum) ⇒ (_type: DealCards.Value) match {
      case DealCards.Board ⇒
        boardCards = cards
        Console printf ("Dealt %s %s\n", _type, Cards(boardCards).toConsoleString)
        
      case _            ⇒
    }
    
    case msg.PlayerJoin(pos, player, amount) =>
      val seat = seats(pos)
      seat.player = player
      seat.buyIn(amount)

    case msg.ButtonChange(pos) ⇒
      Console printf ("Button is %d\n", pos)

    case msg.BetAdd(pos, player, bet) =>
      val seat = seats(pos)
      Console printf ("%s: %s\n", seat.player.get, bet)

    case msg.DeclarePot(total, rake) ⇒
      Console printf ("Pot size: %.2f\nBoard: %s\n", total, Cards(boardCards).toConsoleString)

    case msg.DeclareHand(pos, player, cards, hand) ⇒
      val seat = seats(pos)

      Console printf ("%s has %s (%s)\n", seat.player.get, Cards(cards).toConsoleString, hand.description)

    case msg.DeclareWinner(pos, player, amount) ⇒
      val seat = seats(pos)

      Console printf ("%s won %.2f\n", seat.player.get, amount)
    
    case _: msg.RequireBet =>
      
    case x =>
      Console printf("%sunhandled by play: %s%s\n", Console.RED, x, Console.RESET)
  }
}

object Play {
  case class Join(tableSize: Int, deal: ActorRef)

  val consoleReader = new ConsoleReader
  consoleReader.setExpandEvents(false)

  def readBet(call: Decimal): Bet = {
    var bet: Option[Bet] = None
    while (bet.isEmpty) {
      bet = parseBet(call, consoleReader.readLine(">>> "))
    }
    bet.get
  }

  final val Fold = "fold"
  final val Check = "check"
  final val Call = "call"

  def parseBet(call: Decimal, str: String): Option[Bet] = str match {
    case "" ⇒
      if (call.toDouble == .0) Some(Bet.check)
      else Some(Bet.call(call))
    case Fold  ⇒ Some(Bet.fold)
    case Check ⇒ Some(Bet.check)
    case Call  ⇒ Some(Bet.call(call))
    case _ ⇒
      val parts = str.split(" ")

      val amountStr: String = if (parts.size == 1) parts.head
      else if (parts.size == 2 && parts.head == "raise") parts.last
      else ""

      if (amountStr == "") {
        Console printf ("invalid input: %s", str)
        None
      } else {
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
      val str = consoleReader.readLine
      try {
        cards = Some(Cards(str))
      } catch {
        case _: Card.ParseError ⇒
          cards = None
      }
    }

    cards.get
  }
}
