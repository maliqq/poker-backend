package de.pokerno.gameplay

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }

import de.pokerno.model._
import concurrent._
import de.pokerno.backend.{protocol => message}

class GameplayActor(val gameplay: Gameplay) extends Actor with ActorLogging {
  import context._

  def streets = Streets.build(gameplay, betting)
  lazy val streetsIterator = streets iterator

  var betting: ActorRef = system deadLetters
  var currentStreet: ActorRef = system deadLetters

  val stages: List[RunStage] = List(
    RunStage("prepare-seats") {
      gameplay.prepareSeats
    },
    RunStage("rotate-game") {
      gameplay.rotateGame
    },
    RunStage("post-antes") {
      gameplay.postAntes(betting)
    },
    RunStage("post-blinds") {
      gameplay.postBlinds(betting)
    }

  )

  override def preStart = {
    log.info("start gameplay")
    // FKXME
    gameplay.events.publish(message.PlayStart())
    self ! Street.Start
  }

  def receive = {
    case msg: message.AddBet ⇒
      betting ! msg

    case Street.Start ⇒
      betting = actorOf(Props(classOf[BettingActor], gameplay.round), name = "betting-process")
      for (stage ← stages) {
        stage.run
      }
      self ! Street.Next

    case Street.Next ⇒
      log.info("next street")

      if (streetsIterator hasNext) {
        val Street(name, stages) = streetsIterator.next
        gameplay.events.publish(message.StreetStart(name))
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
    gameplay.events.publish(message.PlayStop())
    parent ! Deal.Done
  }
}
