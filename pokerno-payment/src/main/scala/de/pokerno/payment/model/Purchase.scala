package de.pokerno.payment.model

import org.squeryl.annotations.Column
import org.squeryl.KeyedEntity
import java.util.UUID

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
    @Column("payer_id") var payerId: Long,
    @Column(name="currency_id", optionType=classOf[Long]) var currencyId: Option[Long],
    @Column("order_id") var orderId: Long,
    var state: String,
    @Column("created_at") var created: java.sql.Timestamp = now()
    ) extends Payment(PaymentType.Purchase) {
}

object Order {
  def buyIn(playerId: UUID, amount: Double, itemId: UUID) = {
    val state = "pending"
    val itemType = "Poker::Room"
    val itemTag = "table:buyin"
    new Order(playerId, amount, state, itemId, itemType, itemTag)
  }
  
  def leave(playerId: UUID, amount: Double, itemId: UUID) = {
    val state = "pending"
    val itemType = "Poker::Room"
    val itemTag = "table:leave"
    new Order(playerId, amount, state, itemId, itemType, itemTag)
  }
  
  def register(playerId: UUID, amount: Double, itemId: UUID) = {
    val state = "pending"
    val itemType = "Poker::Tournament"
    val itemTag = "tournament:entry"
    new Order(playerId, amount, state, itemId, itemType, itemTag)
  }
}

sealed class Order(
    @Column("player_id") var playerId: UUID,
    var price: Double,
    var state: String,
    @Column("item_id") var itemId: UUID,
    @Column("item_type") var itemType: String,
    var tag: String) extends KeyedEntity[Long] {
  var id: Long = 0
}
