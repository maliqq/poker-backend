package de.pokerno.finance.model.payment

import org.squeryl.annotations.Column
import org.squeryl.KeyedEntity
import de.pokerno.finance.model.{Balance, Payment, PaymentType}

object Purchase {
  def create(balance: Balance, order: Order) = {
    val state = "pending"
    new Purchase(-order.price, balance.id, order.id, state, java.sql.Timestamp.from(java.time.Instant.now()))
  }
}

/*
 * Оплата за байин или участие в турнире
 * */
sealed class Purchase(
    var amount: Double,
    @Column("payer_id") var payerId: Long,
    @Column("order_id") var orderId: Long,
    var state: String,
    @Column("created_at") var created: java.sql.Timestamp
    ) extends Payment(PaymentType.Purchase) {
}

object Order {
  def buyIn(playerId: java.util.UUID, amount: Double, itemId: java.util.UUID) = {
    val state = "pending"
    val itemType = "Poker::Room"
    val itemTag = "table:buyin"
    new Order(playerId, amount, state, itemId, itemType, itemTag)
  }
  
  def leave(playerId: java.util.UUID, amount: Double, itemId: java.util.UUID) = {
    val state = "pending"
    val itemType = "Poker::Room"
    val itemTag = "table:leave"
    new Order(playerId, amount, state, itemId, itemType, itemTag)
  }
  
  def register(playerId: java.util.UUID, amount: Double, itemId: java.util.UUID) = {
    val state = "pending"
    val itemType = "Poker::Tournament"
    val itemTag = "tournament:entry"
    new Order(playerId, amount, state, itemId, itemType, itemTag)
  }
}

sealed class Order(
    @Column("player_id") var playerId: java.util.UUID,
    var price: Double,
    var state: String,
    @Column("item_id") var itemId: java.util.UUID,
    @Column("item_type") var itemType: String,
    var tag: String) extends KeyedEntity[Long] {
  var id: Long = 0
}
