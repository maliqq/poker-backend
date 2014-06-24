package de.pokerno.payment.model

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column
import java.util.UUID

object Balance {
  
  import de.pokerno.payment.PaymentDB._
  
  def find(playerId: UUID, currencyId: Option[Long]) = {
    from(balances)((balance) =>
      where(balance.playerId === playerId and (
          if (currencyId.isDefined) balance.currencyId === currencyId.get
          else balance.currencyId.isNull))
      select(balance)
    )
  }
  
  
  def get(playerId: UUID, currencyId: Option[Long] = None): Balance = {
    find(playerId, currencyId).head
  }
  
  def getOrCreate(playerId: UUID, currencyId: Option[Long] = None): Balance = {
    find(playerId, currencyId).headOption match {
      case Some(balance) => balance
      case None => create(playerId, currencyId)
    }
  }
  
  def create(playerId: UUID, currencyId: Option[Long] = None, initial: Double = 0): Balance = {
    balances.insert(new Balance(playerId, currencyId, initial))
  }
  
}

sealed class Balance(
    @Column("player_id")var playerId: UUID,
    @Column(name="currency_id", optionType=classOf[Long]) var currencyId: Option[Long],
    var amount: Double,
    @Column(name="in_play", optionType=classOf[Double]) var inPlay: Option[Double] = None
) extends KeyedEntity[Long] {
  var id: Long = 0
  
  import de.pokerno.payment.PaymentDB._
  
  def charge(diff: Double) {
    val n = update(balances)((balance) =>
      where(balance.id === this.id)
      set(balance.amount := balance.amount plus diff)
    )
    if (n == 1) {
      amount += diff
    }
  }
  
  def getRecentBonus(): Option[Bonus] = {
    bonuses.where(b =>
      b.payeeId === id and (if (currencyId.isDefined) {
        b.currencyId === currencyId.get 
      } else b.currencyId.isNull)
    ).headOption
  }
  
}
