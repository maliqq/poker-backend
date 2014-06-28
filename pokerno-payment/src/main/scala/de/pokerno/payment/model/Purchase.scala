package de.pokerno.payment.model

import org.squeryl.annotations.Column
import org.squeryl.KeyedEntity

object Purchase {
  import de.pokerno.payment.PaymentDB._

  def create(balance: Balance, order: Order) = {
    val state = "pending"
    purchases.insert(new Purchase(-order.price, balance.id, balance.currencyId, order.id, state))
  }
}

/*
 * Оплата за байин или участие в турнире
 * */
sealed class Purchase(
    var amount: Double,
    @Column("from_id") var payerId: Long,
    @Column(name="currency_id", optionType=classOf[Long]) var currencyId: Option[Long],
    @Column("order_id") var orderId: Long,
    var state: String,
    @Column("created_at") var created: Timestamp = now()
    ) extends Payment(PaymentType.Purchase) {
}