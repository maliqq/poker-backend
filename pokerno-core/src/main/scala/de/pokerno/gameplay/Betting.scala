package de.pokerno.gameplay

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import de.pokerno.protocol.{msg => message}
import de.pokerno.protocol.rpc
import akka.actor.Actor

object Betting {

  // start new round
  case object Start
  // require bet
  case class Require(amount: Decimal, limit: Game.Limit)
  // force bet
  case class Force(amount: Decimal)
  case class Add(bet: Bet)

  // go to next seat
  case object NextTurn
  // stop current deal
  case object Stop
  // betting done - wait for next street to occur
  case object Done
  // betting timeout - go to next seat
  case object Timeout
  // turn on big bet mode
  case object BigBets

}

