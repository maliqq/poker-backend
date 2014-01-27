package de.pokerno.gameplay

import akka.actor.Actor

trait StreetsBehavior {
  
  a: DealActor =>
  
  import Stages._
  
  def streets: StreetChain

  lazy val beforeStreets =
    stage("prepare-seats") { ctx =>
      ctx.gameplay.prepareSeats(ctx)
      Stage.Next

    } chain stage("rotate-game") { ctx =>
      ctx.gameplay.rotateGame(ctx)
      Stage.Next

    } chain stage("post-antes") { ctx =>
      ctx.gameplay.postAntes(ctx)
      Stage.Next

    } chain stage("post-blinds") { ctx =>
      ctx.gameplay.postBlinds(ctx)
      Stage.Next
    }
  
  lazy val afterStreets =
    stage("showdown") { ctx =>
      ctx.gameplay.showdown
      Stage.Next
    } chain
    
  def handleStreets: Receive = {
    case Betting.Start =>
      log.info("[betting] start")
      gameplay.round.reset
      nextTurn
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
