package de.pokerno.cli

import java.util.Scanner
import math.{ BigDecimal ⇒ Decimal }
import concurrent.duration._
import de.pokerno.model._
import de.pokerno.poker._
import de.pokerno.gameplay._
import de.pokerno.protocol.{msg => message}
import akka.actor.{ Actor, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout

object PlayerActor {
  case object Start
}

class PlayerActor(i: Int, gameplay: Gameplay, instance: ActorRef) extends Actor {
  val stack = 1500.0
  val player = new Player("player-%d".format(i))

  import context._

  def receive = {
    case PlayerActor.Start ⇒
      instance ! message.JoinTable(pos = i - 1, player = player, amount = stack)
      become({
        case message.DealCards(_type, cards, pos, player, cardsNum) ⇒ _type match {
          case DealCards.Hole | DealCards.Door =>
            Console printf ("Dealt %s %s to %d\n", _type, Cards(cards) toConsoleString, pos)
          case DealCards.Board =>
            Console printf ("Dealt %s %s\n", _type, Cards(cards) toConsoleString)
          case _                         ⇒
        }

        case message.RequireBet(pos, player, call, range) ⇒
          Console printf ("Seat %d: Call=%.2f Min=%.2f Max=%.2f\n", pos, call, range.min, range.max)

          val seat = gameplay.table.seats(pos)
          var bet = Play.readBet(call)

          val addBet = message.AddBet(pos, seat.player.get, bet)
          instance ! addBet

        case message.RequireDiscard(pos, player) ⇒
          val seat = gameplay.table.seats(pos)

          Console printf ("your cards: [%s]\n", gameplay.dealer.pocket(seat.player.get))

          val cards = Play.readCards

          instance ! message.DiscardCards(pos, seat.player.get, cards)
      })
  }
}

class Play(gameplay: Gameplay, instance: ActorRef, tableSize: Int) extends Actor {
  import context._

  override def preStart = {
    Console println ("starting play")
    (1 to tableSize) foreach { i ⇒
      val playerActor = system.actorOf(Props(classOf[PlayerActor], i, gameplay, instance), name = "player-process-%d".format(i))
      playerActor ! PlayerActor.Start
    }
    gameplay.events.subscribe(self, "play-observer")
  }

  def receive = {
    case message.DealCards(_type, cards, pos, player, cardsNum) ⇒ _type match {
      case DealCards.Board ⇒ Console printf ("Dealt %s %s\n", _type, Cards(cards) toConsoleString)
      case _            ⇒
    }

    case message.ButtonChange(pos) ⇒
      Console printf ("Button is %d\n", pos)

    case e: message.ActionEvent ⇒ e match {
        case message.AddBet(pos, player, bet) =>
          val seat = gameplay.table.seats(pos)
          Console printf ("%s: %s\n", seat.player.get, bet)
      }

    case message.DeclarePot(total, rake) ⇒
      Console printf ("Pot size: %.2f\nBoard: %s\n", total, Cards(gameplay.dealer.board) toConsoleString)

    case message.DeclareHand(pos, player, cards, hand) ⇒
      val seat = gameplay.table.seats(pos)

      Console printf ("%s has %s (%s)\n", seat.player.get, Cards(cards) toConsoleString, hand description)

    case message.DeclareWinner(pos, player, amount) ⇒
      val seat = gameplay.table.seats(pos)

      Console printf ("%s won %.2f\n", seat.player.get, amount)
  }
}

object Play {
  case class Join(tableSize: Int, deal: ActorRef)

  val sc = new Scanner(System.in)

  def readBet(call: Decimal): Bet = {
    var bet: Option[Bet] = None
    while (bet.isEmpty) {
      Console print (">>> ")

      bet = parseBet(call, sc.nextLine)
    }
    bet.get
  }

  final val Fold = "fold"
  final val Check = "check"
  final val Call = "call"

  def parseBet(call: Decimal, str: String): Option[Bet] = str match {
    case "" ⇒
      if (call == .0) Some(Bet.check)
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
