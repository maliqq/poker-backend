package pokerno.backend.engine

import akka.actor.ActorRef
import akka.actor.actorRef2Scala

case class StageEnv(val gameplay: Gameplay, val streetRef: ActorRef)

object Stage {
  case object Next
}

abstract class Stage {
  def proceed(context: StageEnv) {
    Console.printf("*** [%s] START...\n", name)
    run(context)
    Console.printf("*** [%s] DONE\n", name)
    context.streetRef ! Stage.Next
  }
  
  def name: String
  
  def run(context: StageEnv)
}

abstract class Skippable extends Stage {
  override def proceed(context: StageEnv) {
    Console.printf("*** [%s] START...\n", name)
    run(context)
    Console.printf("*** [%s] DONE...\n", name)
  }
}
