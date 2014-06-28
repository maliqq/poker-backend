package de.pokerno.payment

import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import model._
import java.util.UUID

import de.pokerno.data.pokerdb

object PaymentDB extends Schema {
  
  class Player(var id: UUID) {
  }
  
  val currencies = table[Currency]("currencies")
  
  val balances = table[Balance]("balances")
  val players = table[Player]("players")
  
  //val payments = table[Payment]("payments")
  val deposits = table[Deposit]("payments")
  val withdraws = table[Withdraw]("payments")
  val transfers = table[Transfer]("payments")
  val purchases = table[Purchase]("payments")
  val awards = table[Award]("payments")
  val orders = table[Order]("payment_orders")
  
  val _bonuses = table[Bonus]("payments")
  lazy val bonuses = from(_bonuses)((bonus) =>
    where(bonus.`type` === PaymentType.Bonus)
    select(bonus)
  )
  
  def join(playerId: UUID, amount: Double, roomId: UUID) {
    val stake = pokerdb.model.Room.getStake(roomId)
    val isPlayMoney = stake.currencyId.isEmpty
    val bb = stake.bigBlind
    val (min, max) = (stake.buyInMin * bb, stake.buyInMax * bb)
    // validations
    if (amount < min) {
      throw new thrift.Error("Minimum buy in is: %.2f (%d BB); got: %.2f" format(min, stake.buyInMin, amount))
    }
    if (amount > max) {
      throw new thrift.Error("Maximum buy in is: %.2f (%d BB); got: %.2f" format(max, stake.buyInMax, amount))
    }
    
    inTransaction {
      val balance = Balance.getOrCreate(playerId, stake.currencyId)
      if (balance.amount < amount && isPlayMoney) {
        Refill.refill(balance) // TODO side-effects
      }
      if (balance.amount < amount) {
        throw new thrift.Error("player %s: not enough money; asked: %.2f have: %.2f" format(playerId, amount, balance.amount))
      }
      val order = orders.insert(Order.buyIn(playerId, amount, roomId))
      purchase(balance, order)
    }
  }
  
  case object Refill extends PlayMoney.RefillStrategy
  
  def leave(playerId: UUID, amount: Double, roomId: UUID) {
    val stake = pokerdb.model.Room.getStake(roomId)
    inTransaction {
      val balance = Balance.get(playerId, stake.currencyId)
      val order = orders.insert(Order.leave(playerId, amount, roomId))
      award(balance, order)
    }
  }
  
  def register(playerId: UUID, tournamentId: UUID) {
    val buyIn = pokerdb.model.Tournament.getBuyIn(tournamentId)
    val amount = buyIn.price + buyIn.fee
    // TODO check tournament start date, state
    inTransaction {
      val balance = Balance.get(playerId, buyIn.currencyId)
      val order = orders.insert(Order.register(playerId, amount, tournamentId))
      purchase(balance, order)
    }
  }
  
  def cancelWithdraw(payment: Withdraw) = inTransaction {
    val balance = balances.where((balance) => balance.id === payment.payerId).head
    balance.charge(payment.amount)
    //payment.reject() // rollback if state != "pending"
  }
  
  private def withdraw(balance: Balance, amount: Double) = {
    val payment = Withdraw.create(balance, -amount)
    balance.charge(payment.amount) // block amount for withdraw
  }

  private def deposit(balance: Balance, amount: Double) = {
    //val payment = Deposit.create(balance, amount = amount, state = "approved")
    balance.charge(amount)
  }
  
  private def transfer(from: Balance, to: Balance, amount: Double) = {
    from.charge(-amount)
    to.charge(amount)
    //val payment = Transfer.create(from, to, amount = amount, state = "pending")
  }
  
  private def purchase(balance: Balance, order: Order) = {
    val payment = Purchase.create(balance, order)
    balance.charge(payment.amount)
  }
  
  private def award(balance: Balance, order: Order) = {
    val payment = Award.create(balance, order)
    balance.charge(payment.amount)
  }
  
}
