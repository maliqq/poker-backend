package de.pokerno.gameplay

import akka.actor.{Actor, ActorLogging}

import de.pokerno.model._
import concurrent.duration._

object Deal {

  case object Start
  case object Cancel
  case object Stop
  case class Next(after: FiniteDuration = 5 seconds)
  case object Done
  
}
