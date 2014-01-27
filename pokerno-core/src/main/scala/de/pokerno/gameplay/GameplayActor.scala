package de.pokerno.gameplay

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }

import de.pokerno.model._
import concurrent._
import de.pokerno.protocol.{msg => message}

class GameplayActor(val gameplay: GameplayContext) extends Actor
                                               with ActorLogging
                                               with Stages
                                               with Betting {
  import context._
  
  def streets = Streets.build(gameplay, betting)
  lazy val streetsIterator = streets iterator

  var currentStreet: ActorRef = system deadLetters
  
  val stages = (g: GameplayContext) =>
    stage("prepare-seats")(_.prepareSeats) andThen
    stage("rotate-game")(_.rotateGame) andThen
    stage("post-antes")(_.postAntes(betting)) andThen
    stage("post-blinds")(_.postBlinds(betting))
    
  override def preStart = {
    log.info("start gameplay")
    // FIXME
    gameplay.events.playStart
    self ! Street.Start
  }

  def receive = {
    case msg: message.AddBet ⇒
      betting ! msg

    case Street.Start ⇒
      stages(gameplay)
      become(betting)
      self ! Street.Next

    case Street.Next ⇒
      log.info("next street")

      if (streetsIterator hasNext) {
        val Street(name, stages) = streetsIterator.next
        gameplay.events.streetStart(name)
        currentStreet = actorOf(Props(classOf[StreetActor], gameplay, name, stages), name = "street-%s" format (name))
        currentStreet ! Stage.Next
      } else
        self ! Street.Exit

    case Street.Exit ⇒
      log.info("showdown")
      gameplay.showdown
      stop(self)
  }

  override def postStop {
    log.info("stop gameplay")
    gameplay.events.playStop
    parent ! Deal.Done
  }
}
