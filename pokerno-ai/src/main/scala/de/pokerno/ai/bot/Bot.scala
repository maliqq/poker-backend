package de.pokerno.ai.bot

import de.pokerno.model._
import de.pokerno.poker._
import de.pokerno.gameplay.{Notification, Route}
import de.pokerno.backend.server.Room
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
  var put: Decimal = .0
  var pot: Decimal = .0
  var cards: Cards = Cards.empty
  var board: Cards = Cards.empty
  var stack: Decimal
  def total = stack + put
}

object Action {
  object AllIn
  object Fold
  object CheckCall
  object CheckFold
  object Raise
}

class Bot(room: ActorRef, var pos: Int, var stack: Decimal, var game: Game, var stake: Stake, speed: FiniteDuration)
    extends Actor with Context with Simple {
  
  val id: String = java.util.UUID.randomUUID().toString //f"bot-${pos+1}"
  val player = new Player(id)

  import context._

  override def preStart {
    join()
  }
  
  def join() {
    room ! Room.Subscribe(id)
    room ! cmd.JoinPlayer(pos, player, stack)
  }
  
  def addBet(b: Bet) {
    Console printf ("%s*** BOT #%d: %s%s\n", Console.CYAN, pos, b, Console.RESET)

    room ! cmd.AddBet(player, b)
  }
  
  def receive = {
    case Notification(msg, _, to) =>
      if (to match {
        case Route.All => true
        case Route.One(_id) if _id == id => true
        case _ => false
      }) handleMessage(msg)
  }

  def handleMessage(msg: de.pokerno.protocol.GameEvent) = msg match {
    case message.DeclarePlayStart(play) ⇒
      cards = Cards.empty
      board = Cards.empty
      pot = .0
      opponentsNum = 6

    //case message.GameplayEvent(game: _game, stake: _stake)
    //  game = _game
    //  stake = _stake

    case message.DeclareWinner(position, amount, _) if pos == position.pos ⇒
      stack += amount

    case message.DeclareStreet(name) ⇒
      street = name.toString()

    case message.DeclarePot(_pot, _rake) ⇒
      pot = _pot.total
      put = .0

    case message.DealBoard(_cards) =>
      board ++= (_cards: Cards)
    
    case message.DealHole(position, Left(_cards)) if pos == position.pos =>
      cards ++= (_cards: Cards)
      Console printf ("*** BOT #%d: %s\n", pos, cards)
    
    case message.DealDoor(position, Left(_cards)) if pos == position.pos =>
      cards ++= (_cards: Cards)
      Console printf ("*** BOT #%d: %s\n", pos, cards)

    case message.AskBet(position, call, raise) if pos == position.pos ⇒
      system.scheduler.scheduleOnce(speed) {
        decide(call, raise.getOrElse((.0, .0)))
      }

    case message.DeclareBet(position, _bet, _timeout) if pos == position.pos ⇒
      if (_bet.isActive)
        put = _bet.toActive.amount

    case _ ⇒
  }


  def doCheck() = addBet(Bet.check)
  def doFold() {
    put = .0
    addBet(Bet.fold)
  }

  def doRaise(_amount: Decimal) {
    val amount = _amount.intValue
    stack += put - amount
    put = amount
    addBet(Bet raise amount)
  }

  def doCall(amount: Decimal) {
    stack -= amount
    put += amount
    addBet(Bet call amount)
  }

  def decide(call: Decimal, raise: Tuple2[Decimal, Decimal]) =
    if (cards.size != 2) {
      Console printf ("*** can't decide with cards=%s\n", cards)
      doFold()
    } else {

      val decision = if (board.size == 0) decidePreflop(cards)
      else decideBoard(cards, board)

      Console printf ("*** decision=%s call=%.2f minRaise=%.2f maxRaise=%.2f\n", decision, call, raise._1, raise._2)
      invoke(decision, call, raise)
    }

  def invoke(decision: Decision, call: Decimal, raise: Tuple2[Decimal, Decimal]) {
    val minRaise = raise._1
    val maxRaise = raise._2

    val min = if (call > total) total else call
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
        if (call == put)
          doCheck()
        else
          doFold()

      case Action.CheckCall ⇒
        if (call == put || call.toDouble == .0)
          doCheck()
        else if (call > .0)
          doCall(List(total, call).min - put) // FIXME WTF

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
          doRaise(total)
    }
  }
}
