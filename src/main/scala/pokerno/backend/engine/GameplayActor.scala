package pokerno.backend.engine

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }

class GameplayActor(val gameplay: Gameplay) extends Actor with ActorLogging {
  import context._

  private val streets = Streets.ByGameGroup(gameplay.game.options.group)
  private val streetsIterator = streets iterator

  var betting: ActorRef = system deadLetters
  var currentStreet: ActorRef = system deadLetters

  override def preStart = {
    log.info("start gameplay")

    gameplay.prepareSeats
    gameplay.rotateGame

    self ! Street.Next
  }

  def receive = {
    case Street.Next ⇒
      log.info("next street")

      if (sender != self)
        stop(currentStreet)

      if (streetsIterator hasNext) {
        val Street(name, stages) = streetsIterator.next
        currentStreet = actorOf(Props(classOf[StreetActor], gameplay, betting, name, stages), name = "street-%s" format (name))
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
    parent ! Deal.Done
  }
}
