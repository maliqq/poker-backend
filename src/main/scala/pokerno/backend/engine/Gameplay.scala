package pokerno.backend.engine

import scala.math.{BigDecimal => Decimal}
import pokerno.backend.model._

class Pot {
}

class Betting {
  val DEFAULT_RAISE_COUNT = 9
  var raiseCount: Int = DEFAULT_RAISE_COUNT
  
  private var _bigBets: Boolean = false
  def bigBets = _bigBets
  def turnOnBigBets = {
    _bigBets = true
  }
  
  var pot: Pot = new Pot()
  var betRange: Tuple2[Decimal, Decimal] = (0, 0)
}
