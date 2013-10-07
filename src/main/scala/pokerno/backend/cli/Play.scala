package pokerno.backend.cli

import pokerno.backend.model._
import pokerno.backend.poker._
import pokerno.backend.protocol._
import akka.actor.Actor
import scala.math.{ BigDecimal => Decimal }
import java.util.Scanner

class Play extends Actor {
  def receive = {
    case _ =>
  }
}

object Play {
  val sc = new Scanner(System.in)
  
  def readBet(call: Decimal, toCall: Decimal): Bet = {
    var bet: Option[Bet] = None
    while (bet.isEmpty) {
      Console print(">>> ")
      bet = parseBet(call, toCall, sc.nextLine)
    }
    return bet.get
  }
  
  def parseBet(call: Decimal, toCall: Decimal, str: String): Option[Bet] = str match {
    case "" =>
      if (toCall == .0) Some(Bet.check)
      else Some(Bet.call(toCall))
    case "fold" => Some(Bet.fold)
    case "check" => Some(Bet.check)
    case "call" => Some(Bet.call(toCall))
    case _ =>
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
          case _:java.lang.NumberFormatException => None
        }
      }
  }
  
  def parseCards: List[Card] = {
    var cards: Option[List[Card]] = None
    
    while (cards.isEmpty) {
      val str = sc.nextLine
      try {
        cards = Some(Cards(str))
      } catch {
        case _: Card.ParseError => cards = None
      }
    }
    cards.get
  }
}
