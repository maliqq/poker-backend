package de.pokerno.ai.bot

import de.pokerno.model._
import de.pokerno.poker._
import de.pokerno.backend.Gateway
import de.pokerno.protocol.{msg => message}
import de.pokerno.protocol.cmd
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
  var cards: Cards = Cards.empty
  var board: Cards = Cards.empty
}

object Action {
  object AllIn
  object Fold
  object CheckCall
  object CheckFold
  object Raise
}

class Bot(room: ActorRef, var pos: Int, var stack: Decimal, var game: Game, var stake: Stake)
    extends Actor with Context with Simple {
  
  val id: String = f"bot-${pos+1}"//java.util.UUID.randomUUID().toString
  val player = new Player(id)

  import context._

  override def preStart {
    join()
  }
  
  def join() {
    room ! cmd.JoinPlayer(pos, player, stack)
  }
  
  def addBet(b: Bet) {
    Console printf ("%s*** BOT #%d: %s%s\n", Console.CYAN, pos, b, Console.RESET)

    room ! cmd.AddBet(player, b)
  }

  def receive = {
    case message.DeclarePlayStart() ⇒
      cards = Cards.empty
      board = Cards.empty
      pot = .0
      opponentsNum = 6

    //case message.GameplayEvent(game: _game, stake: _stake)
    //  game = _game
    //  stake = _stake

    case message.DeclareWinner(_pos, winner, amount) if _pos == pos ⇒
      stack += amount

    case message.DeclareStreet(name) ⇒
      street = name.toString()

    case message.DeclarePot(total, _side, _rake) ⇒
      pot = total
      bet = .0

    case message.DealBoard(_cards) =>
      board ++= (_cards: Cards)
    
    case message.DealHole(_pos, player, Left(_cards)) if _pos == pos =>
      cards ++= (_cards: Cards)
      Console printf ("*** BOT #%d: %s\n", pos, cards)
    
    case message.DealDoor(_pos, player, Left(_cards)) if _pos == pos =>
      cards ++= (_cards: Cards)
      Console printf ("*** BOT #%d: %s\n", pos, cards)

    case message.AskBet(_pos, _, call, raise) if _pos == pos ⇒
      system.scheduler.scheduleOnce(1 second) {
        decide(call, raise)
      }

    case message.DeclareBet(_pos, _player, _bet) if _pos == pos ⇒
      _bet match {
        case a: Bet.Active =>
          bet = a.amount
      }

    case _ ⇒
  }


  def doCheck() = addBet(Bet.check)
  def doFold() {
    bet = .0
    addBet(Bet.fold)
  }

  def doRaise(amount: Decimal) {
    stack += bet - amount
    bet = amount
    addBet(Bet raise amount)
  }

  def doCall(amount: Decimal) {
    stack -= amount
    bet += amount
    addBet(Bet call amount)
  }

  def decide(call: Decimal, raise: MinMax[Decimal]) =
    if (cards.size != 2) {
      Console printf ("*** can't decide with cards=%s\n", cards)
      doFold()
    } else {

      val decision = if (board.size == 0) decidePreflop(cards)
      else decideBoard(cards, board)

      Console printf ("*** decision=%s call=%.2f minRaise=%.2f maxRaise=%.2f\n", decision, call, raise.min, raise.max)
      invoke(decision, call, raise)
    }

  def invoke(decision: Decision, call: Decimal, raise: MinMax[Decimal]) {
    val minRaise = raise.min
    val maxRaise = raise.max

    val min = if (call > stack + bet) stack + bet else call
    val max = decision.maxBet

    var action = if (minRaise.toDouble == .0 && maxRaise.toDouble == .0)
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
      case Action.Fold ⇒ doFold()

      case Action.CheckFold ⇒
        if (call == bet)
          doCheck()
        else
          doFold()

      case Action.CheckCall ⇒
        if (call == bet || call.toDouble == .0)
          doCheck()
        else if (call > .0)
          doCall(List(stack + bet, call).min - bet) // FIXME WTF

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
