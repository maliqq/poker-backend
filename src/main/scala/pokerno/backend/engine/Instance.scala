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
case class Gameplay(DealProcess: ActorRef) extends Data

class Instance extends Actor with FSM[State, Data] {

  case object Stop
  case object Pause
  case object Resume
  case object Start
  case class JoinTable
  
  startWith(Created, Empty)
  
  when(Created) {
    case Event(Start, g: Gameplay) =>
      goto(Running) using g
  }

  when(Paused) {
    case Event(Resume, g: Gameplay) =>
      goto(Running) using g
  }
  
  when(Waiting) {
    case Event(join: JoinTable, g: Gameplay) =>
      g.DealProcess ! join
      stay using g
  }
  
  when(Running) {
    case Event(Pause, g: Gameplay) =>
      goto(Paused) using g
  
    case Event(Stop, g: Gameplay) =>
      goto(Closed) using g
  }
  
  onTransition {
    case Created -> Running =>
      stateData match {
        case g: Gameplay =>
          g.DealProcess ! "1"
      }
  }
  
  initialize()
}
