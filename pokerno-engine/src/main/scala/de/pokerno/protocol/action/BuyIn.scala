package de.pokerno.protocol.action

import com.fasterxml.jackson.annotation.{JsonValue, JsonCreator}

object BuyIn {
  @JsonCreator def apply(amount: Number): BuyIn = new BuyIn(amount)
  def unapply(buyIn: BuyIn): Option[Tuple1[Decimal]] = Some(Tuple1(buyIn.amount))
}

sealed class BuyIn(
  val amount: Decimal
) extends PlayerEvent {
  
  def this(amount: Number) = this(amount.doubleValue())
  
}
