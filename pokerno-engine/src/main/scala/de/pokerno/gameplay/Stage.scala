package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.ActorRef
import de.pokerno.protocol.GameEvent

abstract class Stage(val ctx: StageContext) {
  def gameplay  = ctx.gameplay
  def events    = gameplay.events
  def game      = gameplay.game
  def table     = gameplay.table
  def stake     = gameplay.stake
  def round     = gameplay.round
  def dealer    = gameplay.dealer
  
  def process(): Unit
}

private[gameplay] case class StageContext(gameplay: Context, ref: ActorRef) {
  def broadcast(e: GameEvent) = gameplay.events.publish(e) { _.all() }
}

private[gameplay] object Stage {

  trait Control

  case object Next extends Control
  case object Skip extends Control
  case object Wait extends Control
  case object Exit extends Control

}

private[gameplay] object Stages {
  def stage(name: String)(f: StageContext ⇒ Stage.Control): StageTransition = {
    new StageTransition(name, f)
  }
}

private[gameplay] class StageTransition(val name: String, f: StageContext ⇒ Stage.Control) {
  def ~>(f: StageTransition): StageChain =
    new StageChain(this) ~> f

  def ~> =
    new StageChain(this)

  def apply(ctx: StageContext) =
    f(ctx)

  override def toString = f"#[stage:$name]"
}

private[gameplay] class StageChain() {
  var stages = List[StageTransition]()

  def this(stage: StageTransition) = {
    this()
    this ~> stage
  }

  def ~>(stage: StageTransition): StageChain = {
    stages :+= stage
    this
  }

  def current = stages.headOption

  def apply(ctx: StageContext) = {
    var result: Stage.Control = Stage.Next
    if (!stages.isEmpty) {
      stages = stages.dropWhile { f ⇒
        Console printf ("[stage] %s {\n", f.name)
        result = f(ctx)
        Console printf ("[stage] %s }\n", f.name)
        result == Stage.Next
      }
    }
    result
  }

  override def toString = {
    val b = new StringBuilder
    b.append("#[StageChain")
    for (stage ← stages) {
      b.append(" " + stage.toString)
    }
    b.append("]").toString()
  }

}
