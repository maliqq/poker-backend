package de.pokerno.gameplay

private[gameplay] abstract class Stage {
  val ctx: stage.Context
  def apply(): Unit
}

private[gameplay] object Stage {
  trait Control

  case object Next extends Control {
    override def toString = "next"
  }
  case object Wait extends Throwable with Control {
    override def toString = "wait"
  }
  case object Skip extends Throwable with Control {
    override def toString = "skip"
  }
  case object Exit extends Throwable with Control {
    override def toString = "exit"
  }

  type Builder[T <: stage.Context] = stage.Builder[T]
  type Chain[T <: stage.Context] = stage.Chain[T]
  type Context = stage.Context

}
