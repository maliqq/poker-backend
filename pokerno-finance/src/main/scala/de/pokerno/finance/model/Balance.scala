package de.pokerno.finance.model

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column

object Balance {
  def create(playerId: java.util.UUID, currencyId: Option[Long] = None, initial: Double = 0) = {
    new Balance(playerId, currencyId, initial)
  }
}

sealed class Balance(
    @Column("player_id")var playerId: java.util.UUID,
    @Column(name="currency_id", optionType=classOf[Long]) var currencyId: Option[Long],
    var amount: Double
) extends KeyedEntity[Long] {
  var id: Long = 0
  
  import de.pokerno.finance.PaymentDB._
  
  def charge(diff: Double) {
    update(balances)((balance) =>
      where(balance.id === this.id)
      set(balance.amount := balance.amount plus diff)
    )
  }
  
}
