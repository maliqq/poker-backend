package de.pokerno.gameplay.betting

import de.pokerno.model._
import akka.actor.Cancellable
import de.pokerno.gameplay.{ Betting, Deal, Streets }

trait Handler { deal: Deal ⇒
  import context._

  val betting = Betting(ctx, self)

  def handleBetting: Receive = {
    case Betting.Add(player, bet) ⇒
      betting.add(player, bet)

    case Betting.Stop ⇒
      log.info("[betting] stop")
      context.become(handleStreets)
      self ! Streets.Done

    case Betting.Showdown ⇒
      // TODO XXX FIXME WTF?
      //Console printf ("%sgot Betting.Showdown%s\n", Console.RED, Console.RESET)
      context.become(handleStreets)
      self ! Streets.Next

    case Betting.Done ⇒
      log.info("[betting] done")
      betting.doneBets()
      context.become(handleStreets)
      streets.apply()

    case Betting.StartTimer(duration) ⇒
      betting.timer = system.scheduler.scheduleOnce(duration, self, Betting.Timeout)

    case Betting.Timeout ⇒
      betting.timeout()
      self ! nextTurn()

    case Betting.BigBets ⇒
      log.info("[betting] big bets")
      betting.bigBets()
  }
}