package de.pokerno.gameplay

import akka.actor.ActorRef

private[gameplay] object Stages {
  def stage(name: String)(f: StageContext ⇒ Stage.Control): Stage = {
    new Stage(name, f)
  }
}

private[gameplay] case class StageContext(gameplay: Context, ref: ActorRef)

private[gameplay] object Stage {

  trait Control

  case object Next extends Control
  case object Skip extends Control
  case object Wait extends Control
  case object Exit extends Control

}

private[gameplay] class Stage(val name: String, f: StageContext ⇒ Stage.Control) {
  def ~>(f: Stage): StageChain =
    new StageChain(this) ~> f

  def ~> =
    new StageChain(this)

  def apply(ctx: StageContext) =
    f(ctx)

  override def toString = f"#[stage:$name]"
}

private[gameplay] class StageChain() {
  var stages = List[Stage]()

  def this(stage: Stage) = {
    this()
    this ~> stage
  }

  def ~>(stage: Stage): StageChain = {
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
