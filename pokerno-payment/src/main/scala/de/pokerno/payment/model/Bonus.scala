package de.pokerno.payment.model

import org.squeryl.annotations.Column

object Bonus {
  def create(balance: Balance, amount: Double) {
    new Bonus(amount, balance.id, balance.currencyId, "pending")
  }
}

sealed case class Bonus(
    var amount: Double,
    @Column("payee_id") var payeeId: Long,
    @Column(name="currency_id", optionType=classOf[Long]) var currencyId: Option[Long],
    var state: String,
    @Column("created_at") var created: java.sql.Timestamp = now()
) extends Payment(PaymentType.Bonus) {
}
