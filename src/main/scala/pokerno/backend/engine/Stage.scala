package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.poker.{Card, Hand}
import pokerno.backend.protocol._
import scala.math.{BigDecimal => Decimal}

trait Stage {
  def run(context: Gameplay.Context)
}

object Discarding extends Stage {
  def run(context: Gameplay.Context) {
  }
}

class BettingComplete extends Stage {
  def run(context: Gameplay.Context) {
  }
}

object Stages {
  val Default = List[Stage](
  )
}
