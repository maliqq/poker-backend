package pokerno.backend.engine

import akka.actor.{Actor, ActorSystem, ActorRef, Props, FSM}

object Deal {
  case object Start
  case object Stop
}

class Deal extends Actor {
  def receive = {
    case Deal.Start =>
    case Deal.Stop =>
  }
}
