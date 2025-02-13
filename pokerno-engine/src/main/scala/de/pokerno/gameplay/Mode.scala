package de.pokerno.gameplay

import de.pokerno.model.Stake

object Mode extends Enumeration {

  def mode(name: String) = new Val(nextId, name)

  val Cash        = mode("cash")
  val Random      = mode("random")
  val Tournament  = mode("tournament")

}
