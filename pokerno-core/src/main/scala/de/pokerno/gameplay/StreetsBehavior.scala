package de.pokerno.gameplay

import akka.actor.Actor

trait StreetsBehavior {
  
  a: DealActor =>
    
  def streets: StreetChain

  def handleStreets: Receive = {
    case Street.Start =>
      log.info("streets start")
      streets(stageContext)
      
    case Street.Next ⇒
      log.info("streets next")
      streets(stageContext)

    case Street.Exit ⇒
      log.info("streets exit")
      afterStreets(stageContext)
      context.stop(self)
  }

}
