package pokerno.backend.engine

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }
import pokerno.backend.model._
import pokerno.backend.protocol._
import scala.concurrent.duration._

object Deal {
  case object Start
  case object Stop
  case class Next(after: FiniteDuration = 5 seconds)
  case object Done
}
