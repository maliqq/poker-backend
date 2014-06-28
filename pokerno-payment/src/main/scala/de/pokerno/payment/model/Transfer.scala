package de.pokerno.payment.model

import org.squeryl.annotations.Column

object Transfer {

  import de.pokerno.payment.PaymentDB._

  def create(from: Balance, to: Balance, amount: Double) = {
    val state = "pending"
    transfers.insert(new Transfer(amount, from.id, from.currencyId, to.id, state))
  }
  
}

/*
 * Передача денег другому лицу
 * */
sealed class Transfer(
    var amount: Double,
    @Column("from_id") var payerId: Long,
    @Column(name="currency_id", optionType=classOf[Long]) var currencyId: Option[Long],
    @Column("to_id") var payeeId: Long,
    var state: String,
    var created: Timestamp = now()
    ) extends Payment(PaymentType.Transfer) {
}
