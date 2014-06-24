package de.pokerno.payment.model

object Transfer {
  def create(from: Balance, to: Balance, amount: Double, state: String) = {
    new Transfer(amount, from.id, from.currencyId, to.id, state, java.sql.Timestamp.from(java.time.Instant.now()))
  }
}

/*
 * Передача денег другому лицу
 * */
sealed class Transfer(
    var amount: Double,
    var payerId: Long,
    _currencyId: Option[Long],
    var payeeId: Long,
    var state: String,
    var created: java.sql.Timestamp) extends Payment(PaymentType.Transfer, _currencyId) {
}
