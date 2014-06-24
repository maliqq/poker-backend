package de.pokerno.payment.model

object Deposit {
  def create(balance: Balance, amount: Double, state: String) = {
    new Deposit(amount, balance.id, balance.currencyId, state, java.sql.Timestamp.from(java.time.Instant.now()))
  }
}

/*
 * Взнос денег на аккаунт
 * */

sealed class Deposit(
    var amount: Double,
    var payeeId: Long,
    _currencyId: Option[Long],
    var state: String,
    var created: java.sql.Timestamp) extends Payment(PaymentType.Deposit, _currencyId) {
}
