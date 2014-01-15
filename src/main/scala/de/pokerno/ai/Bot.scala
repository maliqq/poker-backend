package de.pokerno.ai

import de.pokerno.model._
import de.pokerno.poker._
import de.pokerno.protocol.{ msg => message }
import math.{ BigDecimal ⇒ Decimal }
import akka.actor.{ Actor, ActorRef }
import util.Random
import concurrent.duration._

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

object Action {
  object AllIn
  object Fold
  object CheckCall
  object CheckFold
  object Raise
}

class Bot(deal: ActorRef, var pos: Int, var stack: Decimal, var game: Game, var stake: Stake)
    extends Actor with Context with Simple {
  val id: String = java.util.UUID.randomUUID().toString
  val player = new Player(id)
  
  import context._

  override def preStart {
    deal ! message.JoinTable(pos = pos, amount = stack, player = player)
  }
  
  def receive = {
    case message.PlayStart() ⇒
      cards = List[Card]()
      board = List[Card]()
      pot = .0
      opponentsNum = 6
    
    //case message.GameplayEvent(game: _game, stake: _stake)
    //  game = _game
    //  stake = _stake

    case message.DeclareWinner(_pos, winner, amount) if (_pos == pos) ⇒
      stack += amount

    case message.StreetStart(name) ⇒
      street = name toString

    case message.DeclarePot(total, _rake) ⇒
      pot = total
      bet = .0

    case message.DealCards(_type, _cards, _pos, _player, _cardsNum) ⇒ _type match {
      case DealCards.Board =>
        board ++= _cards
      case DealCards.Hole | DealCards.Door if (_pos == pos) =>
        cards ++= _cards
        Console printf("*** BOT #%d: %s\n", pos, Cards(cards) toConsoleString)
      case _ =>
    }
    
    case message.RequireBet(_pos, _, call, raise) if (_pos == pos) ⇒
      system.scheduler.scheduleOnce(1 second) {
        decide(call, raise)
      }
    
    case message.AddBet(_pos, _player, _bet) if (_pos == pos) ⇒
      bet = _bet.amount
    
    case _ =>
  }

  def addBet(b: Bet) {
    Console printf ("%s*** BOT #%d: %s%s\n", Console.CYAN, pos, b, Console.RESET)
    
    deal ! message.AddBet(pos, player, b)
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

    var action = if (minRaise == .0 && maxRaise == .0)
      Action.CheckCall
    else if (min > max)
      Action.CheckFold
    else {
      if (Random.nextDouble < decision.raiseChance)
        Action.Raise
      else if (Random.nextDouble < decision.allInChance)
        Action.AllIn
      else
        Action.CheckCall
    }

    action match {
      case Action.Fold      ⇒ doFold
      
      case Action.CheckFold ⇒
        if (call == bet)
          doCheck
        else
          doFold
      
      case Action.CheckCall ⇒
        if (call == bet || call == .0)
          doCheck
        else if (call > .0)
          doCall(call)
      
      case Action.Raise ⇒
        if (minRaise == maxRaise)
          doRaise(maxRaise)
        else {
          val d = maxRaise - minRaise
          val bb = stake.bigBlind
          val amount = minRaise + d * Random.nextDouble
          doRaise(amount)
        }

      case Action.AllIn ⇒
        if (minRaise == maxRaise)
          doRaise(maxRaise)
        else
          doRaise(stack + bet)
    }
  }
}
