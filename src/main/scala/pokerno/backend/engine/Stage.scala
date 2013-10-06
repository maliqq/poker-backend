package pokerno.backend.engine

import akka.actor.ActorRef
import akka.actor.actorRef2Scala

object Stage {
  case object Next
  
  case class Context(val gameplay: Gameplay, val street: ActorRef)
}

abstract class Stage {
  def proceed(context: Stage.Context) {
    Console printf ("*** [%s] START...\n", name)
    run(context)
    Console printf ("*** [%s] DONE\n", name)
    context.street ! Stage.Next
  }

  def name: String

  def run(context: Stage.Context)
}

abstract class Skippable extends Stage {
  override def proceed(context: Stage.Context) {
    Console printf ("*** [%s] START...\n", name)
    run(context)
    Console printf ("*** [%s] DONE...\n", name)
  }
}
