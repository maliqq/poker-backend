package de.pokerno.gameplay.stg

import de.pokerno.gameplay.Stage

private[gameplay] abstract class Step(val name: String) {
  def ~>(f: Step): Chain = new Chain(this) ~> f

  def ~> = new Chain(this)

  def apply(ctx: Context): Stage.Control

  override def toString = f"stage:$name"
}
