package pokerno.backend.engine

<<<<<<< HEAD
import akka.actor.{Actor, Props, ActorSystem, ActorLogging, ActorRef}
=======
import akka.actor.{Actor, ActorSystem, ActorLogging, ActorRef, Props}
>>>>>>> af770ee2c6fa519880c6fa6dd166f41337b04077
import scala.concurrent.Future
import akka.event.Logging
import pokerno.backend.protocol._
import scala.concurrent.duration._

object Deal {
  case object Start
  case object Stop
  case object Done
  
  class Process extends Actor {
    import context.dispatcher
    import context._
    
    var log = Logging(system, this)
    var gameplay: ActorRef
    
    var running: ActorRef = null
    override def preStart {
    }
    
    def receive = {
      case Deal.Start =>
        log.info("deal start")
        running = actorOf(Props(classOf[Gameplay.Process], gameplay))
        running ! Gameplay.Start
      case Message.SitOut =>
      case Message.ComeBack =>
      case Message.AddBet =>
      case Message.ChatMessage =>
      case Message.JoinTable =>
      case Message.LeaveTable =>
      case Message.KickPlayer =>
      
      case Deal.Done =>
        log.info("deal done - starting next deal in 5 seconds")
        system.scheduler.scheduleOnce(5 seconds, self, Deal.Start)
      
      case Deal.Stop =>
        log.info("deal stop")
        stop(self)
    }
    
    override def postStop {
    }
  }
}
