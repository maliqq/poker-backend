package de.pokerno.payment.model

object Withdraw {
  def create(balance: Balance, amount: Double) = {
    val state = "pending"
    new Withdraw(amount, balance.id, balance.currencyId, state, java.sql.Timestamp.from(java.time.Instant.now()))
  }
}

/*
 * Вывод средств со счета
 * */
sealed class Withdraw(
    var amount: Double,
    var payerId: Long,
    _currencyId: Option[Long],
    var state: String,
    var created: java.sql.Timestamp
    ) extends Payment(PaymentType.Withdraw, _currencyId) {
}
