package de.pokerno.backend.ai

import de.pokerno.backend.model._
import de.pokerno.backend.poker._
import de.pokerno.backend.protocol._
import scala.math.{ BigDecimal ⇒ Decimal }
import akka.actor.{ Actor, ActorRef }
import scala.util.Random

trait Context {
  var game: Game
  var stake: Stake

  var opponentsNum: Int = 0
  var street: String = ""
  var bet: Decimal = .0
  var pot: Decimal = .0
  var cards: List[Card] = List.empty
  var board: List[Card] = List.empty
}

class Bot(deal: ActorRef, var pos: Int, var stack: Decimal, var game: Game, var stake: Stake)
    extends Actor with Context with Simple {
  var id: String = java.util.UUID.randomUUID().toString

  override def preStart {
    deal ! Message.JoinTable(pos = pos, amount = stack, player = new Player(id))
  }
  
  def benchmark(name: String)(u: => Unit) {
    val start = System.currentTimeMillis
    u
    Console printf("- %s done in %.2fs\n", name, (System.currentTimeMillis - start) / 1000.0)
  }

  def receive = {
    case msg: Message.PlayStart ⇒
      cards = List[Card]()
      board = List[Card]()
      pot = .0
      opponentsNum = 6
      stake = msg.stake

    case Message.Winner(_pos, winner, amount) if (_pos == pos) ⇒
      stack += amount

    case Message.StreetStart(name) ⇒
      street = name

    case Message.CollectPot(total) ⇒
      pot = total
      bet = .0

    case Message.DealCards(_type, dealt, _pos) ⇒
      _type match {
        case Dealer.Board ⇒
          board ++= dealt
        case Dealer.Hole if (_pos.get == pos) ⇒
          cards ++= dealt
          Console printf("*** BOT #%d: %s\n", pos, Cards(cards) toConsoleString)
      }

    case Message.RequireBet(_pos, call, range) if (_pos == pos) ⇒
       benchmark("decision") { decide(call, range) }

    case Message.AddBet(_pos, _bet) if (_pos == pos) ⇒
      bet = _bet.amount
  }

  def addBet(b: Bet) {
    Console printf ("%s*** BOT #%d: %s%s\n", Console.CYAN, pos, b, Console.RESET)
    
    deal ! Message.AddBet(pos, b)
  }

  def doCheck = addBet(Bet.check)
  def doFold {
    bet = .0
    addBet(Bet.fold)
  }

  def doRaise(amount: Decimal) {
    stack += bet - amount
    bet = amount
    addBet(Bet raise (amount))
  }

  def doCall(amount: Decimal) {
    stack += bet - amount
    bet = amount
    addBet(Bet call (amount))
  }

  def decide(call: Decimal, raise: Range) =
    if (cards.size != 2) {
      Console printf ("*** can't decide with cards=%s\n", Cards(cards) toConsoleString)
      doFold
    } else {

      val decision = if (board.size == 0) decidePreflop(cards)
      else decideBoard(cards, board)

      Console printf ("*** decision=%s call=%.2f minRaise=%.2f maxRaise=%.2f\n", decision, call, raise.min, raise.max)
      invoke(decision, call, raise)
    }

  def invoke(decision: Decision, call: Decimal, raise: Range) {
    val (minRaise, maxRaise) = raise.value

    val min = if (call > stack + bet) stack + bet else call
    val max = decision.maxBet

    var action = if (minRaise == .0 && maxRaise == .0) 'checkCall
    else if (min > max) 'checkFold
    else {
      if (Random.nextDouble < decision.raiseChance) 'raise
      else if (Random.nextDouble < decision.allInChance) 'allIn
      else 'checkCall
    }

    action match {
      case 'fold      ⇒ doFold
      case 'checkFold ⇒ if (call == bet) doCheck else doFold
      case 'checkCall ⇒ if (call == bet || call == .0) doCheck else if (call > .0) doCall(call)
      case 'raise ⇒
        if (minRaise == maxRaise)
          doRaise(maxRaise)
        else {
          val d = maxRaise - minRaise
          val bb = stake.bigBlind
          val amount = minRaise + d * Random.nextDouble
          doRaise(amount)
        }

      case 'allIn ⇒
        if (minRaise == maxRaise)
          doRaise(maxRaise)
        else
          doRaise(stack + bet)
    }
  }
}
