package de.pokerno.gameplay

import akka.actor.ActorRef

object Stages {
  def stage(name: String)(f: StageContext => Stage.Control): Stage = {
    new Stage(name, f)
  }
}

case class StageContext(gameplay: GameplayContext, ref: ActorRef)

object Stage {
  
  trait Control
  
  case object Next extends Control
  case object Skip extends Control
  case object Wait extends Control
  case object Exit extends Control
  
}

class Stage(val name: String, f: StageContext => Stage.Control) {
  def chain(f: Stage): StageChain =
    new StageChain(this) chain f
  
  def chain =
    new StageChain(this)
  
  def apply(ctx: StageContext) =
    f(ctx)
  
  override def toString = f"#[stage:$name]"
}

class StageChain(stage: Stage) {
  var stages = List[Stage](stage)
  
  def chain(stage: Stage): StageChain = {
    stages :+= stage
    this
  }
  
  def current = stages.headOption
  
  def apply(ctx: StageContext) = {
    var result: Stage.Control = Stage.Next
    if (!stages.isEmpty) {
      stages = stages.dropWhile { f =>
        Console printf("[stage] %s {\n", f.name)
        result = f(ctx)
        Console printf("[stage] %s }\n", f.name)
        result == Stage.Next
      }
    }
    result
  }
  
  override def toString = {
    val b = new StringBuilder
    b.append("#[StageChain")
    for(stage <- stages) {
      b.append(" " + stage.toString)
    }
    b.append("]").toString()
  }
  
}
