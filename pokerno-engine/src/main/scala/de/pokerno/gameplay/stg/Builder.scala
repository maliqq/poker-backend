package de.pokerno.gameplay.stg

import de.pokerno.gameplay.Stage

class Builder[Ctx <: Context] {
  
  val chain = new Chain[Ctx]()

  def process(name: String, next: Stage.Control = Stage.Next)(f: Ctx => Unit) = {
    chain ~> new Step[Ctx](name) { def apply(ctx: Ctx): Stage.Control = {
      f(ctx)
      next
    } }
  }
  
  def stage[T <: Stage](name: String)(implicit manifest: Manifest[T]) = {
    chain ~> new Step[Ctx](name) { def apply(ctx: Ctx): Stage.Control = {
      val st: T = manifest.runtimeClass.getConstructor(classOf[Context]).newInstance(ctx).asInstanceOf[T]
      try {
        st.apply()
      } catch {
        case ctl: Stage.Control => return ctl
      }
      Stage.Next
    }}
  }

  def build() = chain

}
