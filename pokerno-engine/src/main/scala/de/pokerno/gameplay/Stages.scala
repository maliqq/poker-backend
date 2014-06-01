package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.ActorRef
import de.pokerno.protocol.GameEvent

abstract class Stage {
  val ctx: StageContext
  def apply(): Unit
}

private[gameplay] case class StageContext(
    gameplay: Context,
    play: Play,
    ref: ActorRef
  ) {
  
  var street: Option[Street.Value] = None
  
  def publish(e: GameEvent)     = gameplay.events.publish(e)_
  def broadcast(e: GameEvent)   = gameplay.events.publish(e) { _.all() }
}

private[gameplay] object Stage {
  trait Control

  case object Next extends Control
  case object Wait extends Throwable with Control
  case object Skip extends Throwable with Control
  case object Exit extends Throwable with Control
}

private[gameplay] object Stages {
  
  def process(name: String, next: Stage.Control = Stage.Next)(f: StageContext => Unit) = {
    new StageStep(name) { def apply(ctx: StageContext): Stage.Control = {
      f(ctx)
      next
    } }
  }
  
  def stage[T <: Stage](name: String)(implicit manifest: Manifest[T]): StageStep =
    new StageStep(name) { def apply(ctx: StageContext): Stage.Control = {
      val st: T = manifest.runtimeClass.getConstructor(classOf[StageContext]).newInstance(ctx).asInstanceOf[T]
      try {
        st.apply()
      } catch {
        case ctl: Stage.Control => return ctl
      }
      Stage.Next
    }}
  
}

private[gameplay] abstract class StageStep(val name: String) {
  def ~>(f: StageStep): StageChain =
    new StageChain(this) ~> f

  def ~> =
    new StageChain(this)

  def apply(ctx: StageContext): Stage.Control

  override def toString = f"stage:$name"
}

private[gameplay] class StageChain() {
  private var _stages = List[StageStep]()
  def stages = _stages
  def current = _stages.headOption
  
  def this(step: StageStep) = {
    this()
    this ~> step
  }

  def ~>(step: StageStep): StageChain = {
    _stages :+= step
    this
  }

  def apply(ctx: StageContext) = {
    var result: Stage.Control = Stage.Next
    if (!stages.isEmpty) {
      _stages = _stages.dropWhile { f ⇒
        Console printf ("[stage] start %s\n", f.name)
        result = f(ctx)
        Console printf ("[stage] stop %s\n", f.name)
        result == Stage.Next
      }
    }
    result
  }
  
  override def toString =
    "stages:" + stages.map(_.toString).mkString("; ")

}
