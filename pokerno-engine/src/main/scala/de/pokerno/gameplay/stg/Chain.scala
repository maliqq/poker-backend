package de.pokerno.gameplay.stg

import de.pokerno.gameplay.Stage

private[gameplay] class Chain {
  private var _stages = List[Step]()
  def stages = _stages
  def current = _stages.headOption
  
  def this(step: Step) = {
    this()
    this ~> step
  }

  def ~>(step: Step): Chain = {
    _stages :+= step
    this
  }

  def apply(ctx: Context) = {
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
