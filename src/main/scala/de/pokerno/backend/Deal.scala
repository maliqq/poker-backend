package de.pokerno.backend
import de.pokerno.model._
import de.pokerno.backend.protocol._
import scala.concurrent.duration._

object Deal {
  case object Start
  case object Stop
  case class Next(after: FiniteDuration = 5 seconds)
  case object Done
}
