package de.pokerno.gameplay.stage

import de.pokerno.gameplay.Stage

private[gameplay] abstract class Step[T <: Context](val name: String) {
  def ~>(f: Step[T]): Chain[T] = new Chain(this) ~> f

  def ~> = new Chain(this)

  def apply(ctx: T): Stage.Control

  override def toString = f"stage:$name"
}
