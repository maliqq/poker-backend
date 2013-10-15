package pokerno.backend.engine

import akka.actor.ActorRef
import akka.actor.actorRef2Scala

object Stage {
  case object Next

  case class Context(val gameplay: Gameplay, val street: ActorRef)
}

abstract class Stage(val name: String)(_run: ⇒ Unit) {
  def run {
    Console printf ("%s*** START %s%s\n", Console.BLUE, name, Console.RESET)
    _run
    Console printf ("%s*** DONE %s%s\n", Console.BLUE, name, Console.RESET)
  }
}

case class RunStage(_name: String)(_run: ⇒ Unit) extends Stage(_name)(_run)

abstract class StreetStage(_name: String)(_run: ⇒ Unit) extends Stage(_name)(_run) {
  def run(street: ActorRef) = {
    super.run
  }
}

case class BlockingStreetStage(_name: String)(_run: ⇒ Unit) extends StreetStage(_name)(_run)

case class DirectStreetStage(_name: String)(_run: ⇒ Unit) extends StreetStage(_name)(_run) {
  override def run(street: ActorRef) {
    super.run(street)
    street ! Stage.Next
  }
}
