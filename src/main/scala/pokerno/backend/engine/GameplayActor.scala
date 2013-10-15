package pokerno.backend.engine

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }
import pokerno.backend.protocol._
import scala.concurrent._

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
    betting = system.actorOf(Props(classOf[BettingActor], gameplay))
    for (stage ← stages) {
      stage.run
    }
    self ! Street.Next
  }

  def receive = {
    case msg: Message.AddBet ⇒
      betting ! msg

    case Street.Next ⇒
      log.info("next street")

      if (streetsIterator hasNext) {
        val Street(name, stages) = streetsIterator.next
        currentStreet = actorOf(Props(classOf[StreetActor], gameplay, name, stages), name = "street-%s" format (name))
        currentStreet ! Stage.Next
      } else
        self ! Street.Exit

    case Street.Exit ⇒
      log.info("showdown")
      stop(self)
  }

  override def postStop {
    log.info("stop gameplay")
    parent ! Deal.Done
  }
}
