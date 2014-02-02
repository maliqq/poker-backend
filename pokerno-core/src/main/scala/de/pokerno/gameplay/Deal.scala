package de.pokerno.gameplay

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }

import de.pokerno.model._
import de.pokerno.protocol.{msg => message}
import concurrent._
import concurrent.duration._

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
  
  override def preStart() {
    log.info("start deal")
    gameplay.events.playStart
    beforeStreets(stageContext) match {
      case Stage.Next => self ! Streets.Next
      case Stage.Exit => // TODO
    }
  }
  
  def receive = handleStreets
  
  override def postStop() {
    log.info("stop deal")
    //afterStreets(stageContext)
    gameplay.events.playStop()
    parent ! Deal.Done
  }
  
}
