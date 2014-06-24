package de.pokerno.finance.model.payment

import de.pokerno.finance.model.{Balance, Payment, PaymentType}

object Deposit {
  def create(balance: Balance, amount: Double, state: String) = {
    new Deposit(amount, balance.id, state, java.sql.Timestamp.from(java.time.Instant.now()))
  }
}

/*
 * Взнос денег на аккаунт
 * */

sealed class Deposit(
    var amount: Double,
    var payeeId: Long,
    var state: String,
    var created: java.sql.Timestamp) extends Payment(PaymentType.Deposit) {
}
