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
    Console printf("TABLE:\n%s\n", table) 
    table.seats.count(_ isReady) == minimumReadyPlayersToStart
  }

}

class Deal(val gameplay: Context) extends Actor
    with ActorLogging
    with Stages.Default {
  
  import context._
  
  val ctx = new stg.Context(gameplay, self)
  val btx = new betting.Context(gameplay, self)
  
  lazy private val onStreets = Streets(ctx)
  
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

  def receive = receiveStreets

  def receiveStreets: Receive = {
    case Betting.Start ⇒
      log.info("[betting] start")
      // FIXME
      //gameplay.round.reset()
      context.become(receiveBets)
      self ! btx.decideNextTurn()

    case Streets.Next ⇒
      log.info("[streets] next")
      onStreets.apply()

    case Streets.Done ⇒
      log.info("[streets] done")
      afterStreets.apply(ctx)
      done()
  }
  
  def receiveBets: Receive = {
    case Betting.Add(player, bet) ⇒
      btx.add(player, bet)

    case Betting.Stop ⇒
      log.info("[betting] stop")
      btx.doneBets()
      context.become(receiveStreets)
      self ! Streets.Done

    case Betting.Showdown ⇒
      // TODO XXX FIXME WTF?
      log.warning("[betting] showdown")
      btx.doneBets()
      context.become(receiveStreets)
      self ! Streets.Next

    case Betting.Done ⇒
      log.info("[betting] done")
      btx.doneBets()
      context.become(receiveStreets)
      onStreets.apply()

    case Betting.StartTimer(duration) ⇒
      btx.timer = system.scheduler.scheduleOnce(duration, self, Betting.Timeout)

    case Betting.Timeout ⇒
      log.info("[betting] timeout")
      btx.timeout()
      self ! btx.decideNextTurn()

    case Betting.BigBets ⇒
      log.info("[betting] big bets")
      btx.bigBets()
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
