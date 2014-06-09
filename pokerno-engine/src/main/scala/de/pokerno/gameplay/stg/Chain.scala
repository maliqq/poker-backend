package de.pokerno.gameplay.stg

import org.slf4j.LoggerFactory
import de.pokerno.gameplay.Stage

private[gameplay] class Chain[T <: Context] {
  val log = LoggerFactory.getLogger(getClass)

  private var _stages = List[Step[T]]()
  def stages = _stages
  def current = _stages.headOption
  
  def this(step: Step[T]) = {
    this()
    this ~> step
  }

  def ~>(step: Step[T]): Chain[T] = {
    _stages :+= step
    this
  }

  def apply(ctx: T) = {
    var result: Stage.Control = Stage.Next
    if (!stages.isEmpty) {
      _stages = _stages.dropWhile { f ⇒
        //Console printf ("[stage] start %s\n", f.name)
        result = f(ctx)
        log.info(f"[stage] ${f.name} -> $result")
        result == Stage.Next
      }
    }
    result
  }
  
  override def toString =
    "stages:" + stages.map(_.toString).mkString("; ")

}
