package pokerno.backend.engine

import akka.actor.{Actor, ActorSystem, ActorLogging, ActorRef, Props}
import scala.concurrent.Future
import akka.event.Logging
import pokerno.backend.protocol._

object Deal {
  case object Start
  case object Stop
  
  class Process extends Actor {
    import context.dispatcher
    import context._
    
    var log = Logging(system, this)
    var gameplay: ActorRef
    
    override def preStart {
    }
    
    def receive = {
      case Deal.Start =>
        log.info("starting new deal")
        gameplay = actorOf(Props[Gameplay.Process])
        gameplay ! Gameplay.Start
        
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
