package de.pokerno.payment.model

import org.squeryl.annotations.Column
import org.squeryl.KeyedEntity

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
