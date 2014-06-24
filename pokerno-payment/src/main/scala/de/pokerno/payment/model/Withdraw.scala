package de.pokerno.payment.model

import org.squeryl.annotations.Column

object Withdraw {

  import de.pokerno.payment.PaymentDB._
  
  def create(balance: Balance, amount: Double) = {
    val state = "pending"
    withdraws.insert(new Withdraw(amount, balance.id, balance.currencyId, state))
  }
}

/*
 * Вывод средств со счета
 * */
sealed class Withdraw(
    var amount: Double,
    @Column("payer_id") var payerId: Long,
    @Column(name="currency_id", optionType=classOf[Long]) var currencyId: Option[Long],
    var state: String,
    @Column("created_at") var created: java.sql.Timestamp = now()
    ) extends Payment(PaymentType.Withdraw) {
}
