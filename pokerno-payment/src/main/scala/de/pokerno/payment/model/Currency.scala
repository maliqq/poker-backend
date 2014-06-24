package de.pokerno.payment.model

import org.squeryl.PrimitiveTypeMode._

object Currency {
  import de.pokerno.payment.PaymentDB._
  
  def getByCode(code: String): Currency = {
    from(currencies)((currency) =>
      where(currency.code === code)
      select(currency)
    ).head
  }
}

sealed class Currency(
    var id: Long,
    var code: String
) {
}
