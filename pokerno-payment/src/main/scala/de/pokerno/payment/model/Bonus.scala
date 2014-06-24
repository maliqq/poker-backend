package de.pokerno.payment.model

import org.squeryl.annotations.Column

object Bonus {
  import de.pokerno.payment.PaymentDB._
  
  def create(balance: Balance, amount: Double) = {
    _bonuses.insert(new Bonus(amount, balance.id, balance.currencyId, "pending"))
  }
}

/*
 * Эмиссия денег
 * */
sealed case class Bonus(
    var amount: Double,
    @Column("payee_id") var payeeId: Long,
    @Column(name="currency_id", optionType=classOf[Long]) var currencyId: Option[Long],
    var state: String,
    @Column("created_at") var created: java.sql.Timestamp = now()
) extends Payment(PaymentType.Bonus) {
}
