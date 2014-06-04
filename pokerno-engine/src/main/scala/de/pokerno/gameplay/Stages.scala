package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.ActorRef
import de.pokerno.protocol.GameEvent

object Stages {
  
  def process(name: String, next: Stage.Control = Stage.Next)(f: stg.Context => Unit) = {
    new stg.Step(name) { def apply(ctx: stg.Context): Stage.Control = {
      f(ctx)
      next
    } }
  }
  
  def stage[T <: Stage](name: String)(implicit manifest: Manifest[T]): stg.Step =
    new stg.Step(name) { def apply(ctx: stg.Context): Stage.Control = {
      val st: T = manifest.runtimeClass.getConstructor(classOf[stg.Context]).newInstance(ctx).asInstanceOf[T]
      try {
        st.apply()
      } catch {
        case ctl: Stage.Control => return ctl
      }
      Stage.Next
    }}
  
}

