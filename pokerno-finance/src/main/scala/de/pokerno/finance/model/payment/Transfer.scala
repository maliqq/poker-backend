package de.pokerno.finance.model.payment

import de.pokerno.finance.model.{Balance, Payment, PaymentType}

object Transfer {
  def create(from: Balance, to: Balance, amount: Double, state: String) = {
    new Transfer(amount, from.id, to.id, state, java.sql.Timestamp.from(java.time.Instant.now()))
  }
}

/*
 * Передача денег другому лицу
 * */
sealed class Transfer(
    var amount: Double,
    var payerId: Long,
    var payeeId: Long,
    var state: String,
    var created: java.sql.Timestamp) extends Payment(PaymentType.Transfer) {
}
