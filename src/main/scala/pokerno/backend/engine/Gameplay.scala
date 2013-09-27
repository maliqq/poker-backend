package pokerno.backend.engine

import scala.math.{BigDecimal => Decimal}
import pokerno.backend.model._

class BettingContext {
  val MaxRaiseCount = 9
  var raiseCount: Int = 0
  
  private var _bigBets: Boolean = false
  def bigBets = _bigBets
  def turnOnBigBets = {
    _bigBets = true
  }
  
  var pot: Pot = new Pot()
  var betRange: Tuple2[Decimal, Decimal] = (0, 0)
}

class BettingProcess {
  
}

class DealContext {
  
}

class DealProcess {
  
}
