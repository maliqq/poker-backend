package pokerno.backend.engine

import akka.actor.{Actor, ActorSystem, ActorRef, Props, FSM}
import scala.concurrent.Future

import pokerno.backend.protocol._

object Deal {
  case object Start
  case object Stop
  
  val Stages: List[Stage] = List(
    
  )
}

class Deal extends Actor {
  import context.dispatcher
  
  val defaultStrategy: List[Stage] = List(
  )
  
  def receive = {
    case Deal.Start =>
      Future {
        defaultStrategy // TODO do something with this
      } onComplete { case _ =>
        self ! Deal.Start
      }
    case Deal.Stop =>
  }
}

class StageProcess extends Actor {
  case class Next
  case class Stop
  case class Exit
  
  case class BettingStart
  case class BettingTimeout
  import context._
  def bettingInProgress: Receive = {
    case BettingStart =>
    case BettingTimeout =>
    case msg: Message.AddBet =>
  }
  
  def receive = {
    case Next =>
    case Exit =>
  }
}
