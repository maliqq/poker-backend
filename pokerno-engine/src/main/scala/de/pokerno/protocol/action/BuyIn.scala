package de.pokerno.protocol.action

import com.fasterxml.jackson.annotation.JsonValue

sealed case class BuyIn(
  @JsonValue val amount: Decimal
) extends PlayerEvent {
  
  def this(amount: Int) = this(amount: Decimal)
  def this(amount: Double) = this(amount: Decimal)
  
}
