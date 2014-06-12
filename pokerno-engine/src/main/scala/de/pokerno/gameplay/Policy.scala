package de.pokerno.gameplay

import de.pokerno.model.Stake
import math.{BigDecimal => Decimal}

object Mode extends Enumeration {

  def mode(name: String) = new Val(nextId, name)

  val Cash        = mode("cash")
  val Random      = mode("random")
  val Tournament  = mode("tournament")

}

object BuyIn {

  case class Policy(min: Int, max: Int) {
    
    def apply(stake: Stake): Tuple2[Decimal, Decimal] = {
      (stake.bigBlind * min, stake.bigBlind * max)
    }
    
  }
  
}
