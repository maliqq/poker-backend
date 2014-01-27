package de.pokerno.gameplay

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }

import de.pokerno.model._
import concurrent._
import de.pokerno.protocol.{msg => message}

class DealActor(val gameplay: GameplayContext) extends Actor
                                               with ActorLogging
                                               with BettingBehavior
                                               with StreetsBehavior {
  import context._
  import Stages._
  
  lazy val streets = Streets(stageContext)
  lazy val stageContext = StageContext(gameplay, self)
  
  override def preStart = {
    log.info("start gameplay")
    gameplay.events.playStart
    beforeStreets(stageContext) match {
      case Stage.Next => self ! Streets.Next
    }
  }
    
  def receive = handleStreets

  override def postStop {
    log.info("stop gameplay")
    //afterStreets(stageContext)
    gameplay.events.playStop
    parent ! Deal.Done
  }
  
}
