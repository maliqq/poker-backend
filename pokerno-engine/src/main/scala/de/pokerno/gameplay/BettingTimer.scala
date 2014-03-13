package de.pokerno.gameplay

import akka.actor.{ Actor, Cancellable }
import concurrent.duration._

trait BettingTimer {

  a: Actor ⇒

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
        self ! Betting.Timeout
      }
    }
  }

}
