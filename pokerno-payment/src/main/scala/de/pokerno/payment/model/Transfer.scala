package de.pokerno.payment.model

import org.squeryl.annotations.Column

object Transfer {

  import de.pokerno.payment.PaymentDB._

  def create(from: Balance, to: Balance, amount: Double, state: String) = {
    transfers.insert(new Transfer(amount, from.id, from.currencyId, to.id, state))
  }
  
}

/*
 * Передача денег другому лицу
 * */
sealed class Transfer(
    var amount: Double,
    var payerId: Long,
    @Column(name="currency_id", optionType=classOf[Long]) var currencyId: Option[Long],
    var payeeId: Long,
    var state: String,
    var created: java.sql.Timestamp = now()
    ) extends Payment(PaymentType.Transfer) {
}
