package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.poker.{Card, Hand}
import pokerno.backend.protocol._
import scala.math.{BigDecimal => Decimal}

trait Context

trait Stage {
  def run(context: Context)
}

class StageContainer(var context: Context = null) extends Runnable {
  this : Stage =>
    def run: Unit = run(context)
}


object Discarding extends Stage {
  def run(context: Context) {
  }
}

class BettingComplete extends Stage {
  def run(context: Context) {
  }
}

object Stages {
  val Default = List[Stage](
  )
}
