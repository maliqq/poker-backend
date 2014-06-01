package de.pokerno.gameplay

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }
import de.pokerno.model._
import de.pokerno.poker.Card
import concurrent._
import concurrent.duration._
import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.gameplay.betting.NextTurn

object Deal {

  case object Start
  case object Cancel
  case object Stop
  case class Next(after: FiniteDuration = 5 seconds)
  case object Done

}

trait DealCycle { a: Actor ⇒

  import context._

  final val minimumReadyPlayersToStart = 2
  final val firstDealAfter = (10 seconds)
  final val nextDealAfter = (5 seconds)

  def table: Table

  protected def canStart: Boolean = {
    table.seats.count(_ isReady) == minimumReadyPlayersToStart
  }

}

class Deal(val gameplay: Context, val play: Play) extends Actor
    with ActorLogging
    with betting.Handler
    with NextTurn
    with Streets.Default {
  
  import context._
  
  val ctx = StageContext(gameplay, new Play, self)
  lazy val streets = Streets(ctx)
  
  override def preStart() {
    log.info("[deal] start")
    beforeStreets.apply(ctx) match {
      case Stage.Next ⇒
        self ! Streets.Next
      case Stage.Exit ⇒
        cancel()
    }
  }

  override def postStop() {
  }

  def receive = handleStreets

  def handleStreets: Receive = {
    case Betting.Start ⇒
      log.info("[betting] start")
      // FIXME
      //gameplay.round.reset()
      context.become(handleBetting)
      self ! nextTurn()

    case Streets.Next ⇒
      log.info("streets next")
      streets.apply()

    case Streets.Done ⇒
      log.info("streets done")
      afterStreets.apply(ctx)
      done()
  }

  private def cancel() {
    log.info("[deal] cancel")
    parent ! Deal.Cancel
    context stop self
  }

  private def done() {
    log.info("[deal] done")
    parent ! Deal.Done
    context stop self
  }

}
