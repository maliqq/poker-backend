package pokerno.backend.ai

import pokerno.backend.model._
import pokerno.backend.poker._
import pokerno.backend.protocol._
import scala.math.{BigDecimal => Decimal}
import akka.actor.{Actor, ActorRef}
import scala.util.Random

trait Context {
  var opponentsNum: Int
  var game: Game
  var stake: Stake
  var street: String
  var bet: Decimal
  var pot: Decimal
  var cards: List[Card]
  var board: List[Card]
}

abstract class Bot extends Actor with Context with Simple {
  var id: String = java.util.UUID.randomUUID().toString
  var room: ActorRef
  var pos: Int
  var stack: Decimal

  def join(at: Int, amount: Decimal) {
    pos = at
    stack = amount
  
    Console.printf("joining table...")
  
    room ! Message.JoinTable(pos = pos, amount = amount, player = new Player("x"))
  }

  def receive = {
    case msg: Message.PlayStart =>
      cards = List[Card]()
      board = List[Card]()
      pot = 0.
      opponentsNum = 6
      stake = msg.stake
  
    case msg: Message.StreetStart =>
      street = msg.name

    case msg: Message.CollectPot =>
      pot = msg.total
      bet = 0.

    case msg: Message.DealCards =>
      msg._type match {
      case Dealer.Board =>
        board ++= msg.cards
      case Dealer.Hole =>
        cards ++= msg.cards
      }

    case msg: Message.RequireBet =>
      if (msg.pos == pos) {
        //<-time.After(1 * time.Second)
        decide(msg)
      }

    case msg: Message.AddBet =>
      if (msg.pos == pos)
        bet = msg.bet.amount
  }

  def addBet(b: Bet) {
    Console.printf("=== %s", b)
  }
  
  def doCheck = addBet(Bet.check)
  def doFold {
    bet = 0.
    addBet(Bet.fold)
  }
  
  def doRaise(amount: Decimal) {
    stack += bet - amount
    bet = amount
    addBet(Bet.raise(amount))
  }
  
  def doCall(amount: Decimal) {
    stack += bet - amount
    bet = amount
    addBet(Bet.call(amount))
  }
  
  def decide(msg: Message.RequireBet) {
    if (cards.size != 2) {
      Console.printf("*** can't decide with cards=%s", cards)
      doFold
      return
    }
  
    val decision = if (board.size == 0) decidePreflop(cards)
    else decideBoard(cards, board)
    
    invoke(decision, msg)
  }
  
  def invoke(decision: Decision, msg: Message.RequireBet) {
    val (call, minRaise, maxRaise) = (msg.call, msg.min, msg.max)
    
    Console.printf("decision=%#v call=%.2f minRaise=%.2f maxRaise=%.2f", decision, call, minRaise, maxRaise)
  
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
    case 'fold => doFold
    case 'checkFold => if (call == bet) doCheck else doFold
    case 'checkCall => if (call == bet || call == .0) doCheck else if (call > .0) doCall(call)
    case 'raise =>
      if (minRaise == maxRaise)
        doRaise(maxRaise)
      else {
        val d = maxRaise - minRaise
        val bb = stake.bigBlind
        val amount = minRaise + d * Random.nextDouble
        doRaise(amount)
      }
      
    case 'allIn =>
      if (minRaise == maxRaise)
        doRaise(maxRaise)
      else
        doRaise(stack + bet)
    }
  }
}
