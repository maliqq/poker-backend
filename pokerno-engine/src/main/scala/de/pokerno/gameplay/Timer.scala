package de.pokerno.gameplay

import akka.actor.Cancellable
import concurrent.duration.FiniteDuration

object Timer {

  trait Value
  case object CheckFold extends Value
  case object SitOut extends Value
  case object Kick extends Value
  
}

case class Timer(val value: Timer.Value, t: Cancellable) {
  def cancel() = t.cancel()
}
