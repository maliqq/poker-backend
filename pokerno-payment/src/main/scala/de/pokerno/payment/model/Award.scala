package de.pokerno.payment.model

import org.squeryl.annotations.Column
import org.squeryl.KeyedEntity

object Award {
  import de.pokerno.payment.PaymentDB._

  def create(balance: Balance, order: Order) = {
    val state = "pending"
    awards.insert(new Award(order.price, balance.id, balance.currencyId, order.id, state))
  }
}

/*
 * Выдача средств (вывод со стола или выигрыш в турнире)
 * */
sealed class Award(
    var amount: Double,
    @Column("to_id") var payeeId: Long,
    @Column(name="currency_id", optionType=classOf[Long]) var currencyId: Option[Long],
    @Column("order_id") var orderId: Long,
    var state: String,
    @Column("created_at") var created: Timestamp = now()
    ) extends Payment(PaymentType.Award) {
}
