package de.pokerno.gameplay

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }

import de.pokerno.model._
import concurrent._
import de.pokerno.protocol.{msg => message}

class DealActor(val gameplay: GameplayContext) extends Actor
                                               with ActorLogging
                                               with Stages
                                               with BettingBehavior
                                               with StreetsBehavior {
  import context._
  
  lazy val beforeStreets =
    stage("prepare-seats") { ctx =>
      
      ctx.gameplay.prepareSeats(ctx)

    } chain stage("rotate-game") { ctx =>
      
      ctx.gameplay.rotateGame(ctx)

    } chain stage("post-antes") { ctx =>

      ctx.gameplay.postAntes(ctx)

    } chain stage("post-blinds") { ctx =>
      
      ctx.gameplay.postBlinds(ctx)

    }
  
  lazy val afterStreets =
    stage("showdown") { ctx =>
      
      ctx.gameplay.showdown
      
    } chain
  
  lazy val streets = Streets(stageContext)
  
  override def preStart = {
    log.info("start gameplay")
    gameplay.events.playStart
    beforeStreets(stageContext)
  }

  override def postStop {
    afterStreets(stageContext)
    log.info("stop gameplay")
    gameplay.events.playStop
    parent ! Deal.Done
  }
  
  def stageContext = StageContext(gameplay, self)
}
