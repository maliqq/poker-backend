package de.pokerno.finance.model.payment

import de.pokerno.finance.model.{Balance, Payment, PaymentType}

object Withdraw {
  def create(balance: Balance, amount: Double) = {
    val state = "pending"
    new Withdraw(amount, balance.id, state, java.sql.Timestamp.from(java.time.Instant.now()))
  }
}

/*
 * Вывод средств со счета
 * */
sealed class Withdraw(
    var amount: Double,
    var payerId: Long,
    var state: String,
    var created: java.sql.Timestamp
    ) extends Payment(PaymentType.Withdraw) {
}
