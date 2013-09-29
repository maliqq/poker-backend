package pokerno.backend.engine

import akka.actor.{Actor, ActorSystem, ActorRef, Props, FSM}

sealed trait State

case object Created extends State
case object Waiting extends State
case object Running extends State
case object Paused extends State
case object Closed extends State

sealed trait Data
case object Empty extends Data

object Instance {
  case object Stop
  case object Pause
  case object Resume
  case object Start
  case class JoinTable
}

class Instance extends Actor with FSM[State, Data] {

  
  startWith(Created, Empty)
  
  when(Created) {
    case Event(Instance.Start, g: Gameplay) =>
      goto(Running) using g
  }

  when(Paused) {
    case Event(Instance.Resume, g: Gameplay) =>
      goto(Running) using g
  }
  
  when(Waiting) {
    case Event(join: Instance.JoinTable, g: Gameplay) =>
      stay using g
  }
  
  when(Running) {
    case Event(Instance.Pause, g: Gameplay) =>
      goto(Paused) using g
  
    case Event(Instance.Stop, g: Gameplay) =>
      goto(Closed) using g
  }
  
  onTransition {
    case Created -> Running =>
      stateData match {
        case g: Gameplay =>
        case _ =>
      }
  }
  
  initialize()
}
