package de.pokerno.gameplay

import scala.math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._

import scala.math.{BigDecimal => Decimal}

object Betting {
  // start new round
  case object Start
  // require bet
  case class Require(amount: Decimal, limit: Game.Limit)
  // force bet
  case class Force(amount: Decimal)

  // go to next seat
  case object Next
  // stop current deal
  case object Stop
  // betting done - wait for next street to occur
  case object Done
  // betting timeout - go to next seat
  case object Timeout
  // turn on big bet mode
  case object BigBets
}
