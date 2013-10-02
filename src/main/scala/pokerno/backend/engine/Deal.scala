package pokerno.backend.engine

import akka.actor.{Actor, ActorLogging, ActorRef}
import scala.concurrent.Future
import akka.event.Logging
import pokerno.backend.protocol._

object Deal {
  case object Start
  case object Stop
  
  class Process(val gameplay: Gameplay) extends Actor {
    import context.dispatcher
    import context._
    
    var log = Logging(system, this)
    
    override def preStart {
    }
    
    def receive = {
      case Deal.Start =>
        log.info("starting new deal")
        Future{
          gameplay.run(self)
        } onSuccess { case _ =>
          log.info("deal success")
          self ! Deal.Start
        }
      case Message.SitOut =>
      case Message.ComeBack =>
      case Message.AddBet =>
      case Message.ChatMessage =>
      case Message.JoinTable =>
      case Message.LeaveTable =>
      case Message.KickPlayer =>
      case Deal.Stop =>
        stop(self)
    }
    
    override def postStop {
      log.info("deal done.")
    }
  }
}
