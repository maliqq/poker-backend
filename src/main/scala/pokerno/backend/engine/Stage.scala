package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.poker.{Card, Hand}
import pokerno.backend.protocol._
import scala.math.{BigDecimal => Decimal}

trait ctx

trait Stage[T <: ctx] {
  def run(context: T)
}

class StageContainer[T <: ctx](var context: T) extends Runnable {
  this : Stage =>
    def run: Unit = run(context)
}


object Discarding extends Stage[Context] {
  class Context extends ctx {
    
  }
  
  def run(context: Context) {
  }
}

class BettingComplete extends Stage[Gameplay.Context] {
  def run(context: ctx) {
  }
}

object Stages {
  val Default = List[Stage[ctx]](
  )
}
