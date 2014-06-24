package de.pokerno.payment.model

import org.squeryl.annotations.Column

object Deposit {

  import de.pokerno.payment.PaymentDB._

  def create(balance: Balance, amount: Double, state: String) = {
    deposits.insert(new Deposit(amount, balance.id, balance.currencyId, state))
  }
}

/*
 * Взнос денег на аккаунт
 * */

sealed class Deposit(
    var amount: Double,
    @Column("to_id") var payeeId: Long,
    @Column(name="currency_id", optionType=classOf[Long]) var currencyId: Option[Long],
    var state: String,
    @Column("created_at") var created: java.sql.Timestamp = now()
    ) extends Payment(PaymentType.Deposit) {
}
