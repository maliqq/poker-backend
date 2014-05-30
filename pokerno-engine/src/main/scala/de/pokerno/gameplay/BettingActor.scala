package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.Cancellable

class BettingActor extends Betting with NextTurn {
    deal: Deal ⇒

    import context._

    var timer: Cancellable = null

    val timers = collection.mutable.HashMap[Player, Timer]()

    def handleBetting: Receive = {
      case Betting.Add(player, bet) ⇒
        val pos = gameplay.round.current
        val seat = gameplay.table.seats(pos)
        if (seat.player == player) {
          if (timer != null)
            timer.cancel()
          log.info("[betting] add {}", bet)
          gameplay.addBet(stageContext, bet)
          self ! nextTurn()
        } else
          log.warning("[betting] not a turn of {}; current acting is {}", player, seat.player)

      case Betting.Stop ⇒
        log.info("[betting] stop")
        context.become(handleStreets)
        self ! Streets.Done

      case Betting.Showdown ⇒
        // TODO XXX FIXME
        Console printf ("%sgot Betting.Showdown%s\n", Console.RED, Console.RESET)
        context.become(handleStreets)
        self ! Streets.Next

      case Betting.Done ⇒
        log.info("[betting] done")
        gameplay.completeBetting(stageContext)
        context.become(handleStreets)
        streets(stageContext)

      case Betting.StartTimer(duration) ⇒
        timer = system.scheduler.scheduleOnce(duration, self, Betting.Timeout)

      case Betting.Timeout ⇒
        val round = gameplay.round
        val (seat, pos) = round.acting.get

        val bet: Bet = seat.state match {
          case Seat.State.Away ⇒
            // force fold
            Bet.fold(timeout = true)

          case _ ⇒
            // force check/fold
            if (round.call == 0 || seat.didCall(round.call))
              Bet.check(timeout = true)
            else Bet.fold(timeout = true)
        }

        log.info("[betting] timeout")
        gameplay.addBet(stageContext, bet)
        self ! nextTurn()

      case Betting.BigBets ⇒
        log.info("[betting] big bets")
        gameplay.round.bigBets = true
    }

  }
}