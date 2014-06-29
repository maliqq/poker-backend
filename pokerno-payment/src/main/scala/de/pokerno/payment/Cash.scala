package de.pokerno.payment

import org.squeryl.PrimitiveTypeMode._

import model._
import de.pokerno.data.pokerdb

/*
Игра на деньги
*/
object Cash {

  import PaymentDB._

  case object Refill extends PlayMoney.RefillStrategy
  
  def join(roomId: UUID, playerId: UUID, amount: Double) {
    val stake = pokerdb.model.Room.getStake(roomId)
    val isPlayMoney = stake.currencyId.isEmpty
    val bb = stake.bigBlind
    val (min, max) = (stake.buyInMin * bb, stake.buyInMax * bb)
    // validations
    if (amount < min) {
      throw new thrift.BuyInRequired("Minimum buy in is: %.2f (%d BB); got: %.2f" format(min, stake.buyInMin, amount))
    }
    if (amount > max) {
      throw new thrift.BuyInRequired("Maximum buy in is: %.2f (%d BB); got: %.2f" format(max, stake.buyInMax, amount))
    }
    
    inTransaction {
      val balance = Balance.getOrCreate(playerId, stake.currencyId)
      if (balance.amount < amount && isPlayMoney) {
        Refill.refill(balance) // TODO side-effects
      }
      if (balance.amount < amount) {
        throw new thrift.NotEnoughMoney("player %s: not enough money; asked: %.2f have: %.2f" format(playerId, amount, balance.amount))
      }
      val order = orders.insert(Order.buyIn(playerId, amount, roomId))
      purchase(balance, order)
    }
  }
  
  def leave(roomId: UUID, playerId: UUID, amount: Double) {
    val stake = pokerdb.model.Room.getStake(roomId)
    inTransaction {
      val balance = Balance.get(playerId, stake.currencyId)
      val order = orders.insert(Order.leave(playerId, amount, roomId))
      award(balance, order)
    }
  }

}
