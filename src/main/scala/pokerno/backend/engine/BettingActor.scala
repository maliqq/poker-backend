package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import scala.math.{ BigDecimal ⇒ Decimal }
import akka.actor.{ Actor, ActorRef, ActorLogging }

class BettingActor(gameplay: Gameplay) extends Actor with ActorLogging {
  import context._

  val pot = new Pot

  var bigBets: Boolean = false

  final val MaxRaiseCount = 8
  private var raiseCount: Int = 0

  private var _call: Decimal = .0
  private var _raise: Range = (.0, .0)

  def round = gameplay.round

  def receive = {
    case Message.AddBet(pos, bet) ⇒
      val (seat, pos) = round.acting

      if (bet.isValid(seat.amount, seat.put, _call, _raise))
        seat post (bet)
      else
        seat fold

      if (bet.betType == Bet.Raise)
        raiseCount += 1

      if (bet.betType != Bet.Call && bet.amount > .0)
        _call = bet.amount

      if (seat.state == Seat.AllIn)
        pot <<- (seat.player.get, seat.put)
      else
        pot << (seat.player.get, seat.put)

    case Betting.Start ⇒
      raiseCount = 0
      _call = .0
      _raise = (.0, .0)
      self ! Betting.Next

    case Betting.Require(amount, limit) ⇒
      val (seat, pos) = round acting
      val stack = seat amount

      if (stack < _call)
        _raise = (.0, .0)
      else {
        var (min, max) = limit raise (stack, amount, pot total)
        min += _call
        max += _call
        _raise = Range(List(stack, min) min, List(stack, max) min)
      }

      gameplay requireBet (_call, _raise)

    case Betting.Next ⇒
      round.move
      round.seats where (_ inPlay) foreach {
        case (seat, pos) ⇒
          if (!seat.isCalled(_call))
            seat check
      }

      if (round.seats.where(_ inPot).size < 2)
        self ! Betting.Stop
      else {
        val active = round.seats where (_ isPlaying)
        if (active.size == 0)
          self ! Betting.Done
        else {
          round acting = active.head
          val stake = gameplay.stake
          val amount = if (bigBets) stake.bigBlind else stake.bigBlind * 2
          self ! Betting.Require(amount, gameplay.game.limit)
        }
      }

    case Betting.Stop ⇒
      gameplay showdown (pot)
      parent ! Street.Exit
      stop(self)

    case Betting.Timeout ⇒
      self ! Betting.Next

    case Betting.BigBets ⇒ bigBets = true

    case Betting.Done ⇒
      gameplay bettingComplete (pot)
      parent ! Street.Next
  }

}
