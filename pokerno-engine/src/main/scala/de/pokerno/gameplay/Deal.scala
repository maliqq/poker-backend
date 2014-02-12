package de.pokerno.gameplay

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }
import de.pokerno.model._
import de.pokerno.poker.Card
import de.pokerno.protocol.{ msg ⇒ message }
import concurrent._
import concurrent.duration._
import math.{ BigDecimal ⇒ Decimal }

object Deal {

  case object Start
  case object Cancel
  case object Stop
  case class Next(after: FiniteDuration = 5 seconds)
  case object Done

}

class Deal(val gameplay: Context) extends Actor
    with ActorLogging
    with Betting.DealContext
    with Streets.DealContext {
  import context._

  lazy val streets = Streets(stageContext)
  lazy val stageContext = StageContext(gameplay, self)

  val play = new Play(gameplay)
  play.getStreet = () ⇒ streets.current.value

  override def preStart() {
    log.info("start deal")
    gameplay.events.playStart
    //play.started() // FIXME ugly
    beforeStreets(stageContext) match {
      case Stage.Next ⇒ self ! Streets.Next
      case Stage.Exit ⇒ context stop self
    }
  }

  override def postStop() {
    log.info("stop deal")
    //afterStreets(stageContext)
    gameplay.events.playStop()
    play.finished() // FIXME ugly
    parent ! Deal.Done
  }

  def receive = handleStreets

  def handleStreets: Receive = {
    case Betting.Start ⇒
      log.info("[betting] start")
      // FIXME
      //gameplay.round.reset()
      nextTurn() //.foreach(self ! _)
      context.become(handleBetting)

    case Streets.Next ⇒
      log.info("streets next")
      streets(stageContext)

    case Streets.Done ⇒
      log.info("streets done")
      afterStreets(stageContext)
      context.stop(self)
  }

}
