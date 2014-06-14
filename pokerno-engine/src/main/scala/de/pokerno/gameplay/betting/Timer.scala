package de.pokerno.gameplay.betting

import akka.actor.{ Actor, Cancellable }
import concurrent.duration._
import de.pokerno.gameplay.{Betting, Round => GameplayRound}

trait Timer { a: Actor ⇒

  import context._

  var maxTicks: Long = 30
  var timer: Cancellable = null
  var ticks: Long = 0

  def startTimer() {
    timer = system.scheduler.schedule(1 seconds, 1 seconds) {
      ticks += 1
      if (ticks >= maxTicks) {
        timer.cancel()
        ticks = 0
        self ! GameplayRound.Timeout
      }
    }
  }

}
