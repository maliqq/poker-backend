package pokerno.backend.engine

import akka.actor.{ Actor, Props, ActorSystem, ActorLogging, ActorRef }
import scala.concurrent.Future
import akka.event.Logging
import pokerno.backend.protocol._
import scala.concurrent.duration._

object Deal {
  case object Start
  case object Stop
  case object Done
}

class DealActor(val gameplay: Gameplay) extends Actor {
  import context.dispatcher
  import context._

  var log = Logging(system, this)

  var running: ActorRef = system deadLetters
  override def preStart {
  }

  def receive = {
    case Deal.Start ⇒
      log.info("deal start")
      running = actorOf(Props(classOf[GameplayActor], gameplay))

    case Message.SitOut      ⇒
    case Message.ComeBack    ⇒
    case Message.AddBet      ⇒
    case Message.ChatMessage ⇒
    case Message.JoinTable   ⇒
    case Message.LeaveTable  ⇒
    case Message.KickPlayer  ⇒

    case Deal.Done ⇒
      log.info("deal done - starting next deal in 5 seconds")
      system.scheduler scheduleOnce (5 seconds, self, Deal.Start)

    case Deal.Stop ⇒
      log.info("deal stop")
      stop(self)
  }

  override def postStop {
  }
}
