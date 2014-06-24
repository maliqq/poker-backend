package de.pokerno.payment

import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import model._
import java.util.UUID

import de.pokerno.data.pokerdb.PokerDB

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
  val orders = table[Order]("payment_orders")
  
  // returns balance of specified currency for player playerId
  def createBalance(playerId: UUID, currencyId: Option[Long] = None, initial: Double = 0): Balance = {
    balances.insert(Balance.create(playerId, currencyId, initial))
  }
  
  def getCurrencyByCode(code: String): Currency = {
    from(currencies)((currency) =>
      where(currency.code === code)
      select(currency)
    ).head
  }
  
  def getBalance(playerId: UUID, currencyId: Option[Long] = None): Balance = {
    // FIXME handle not found
    from(balances)((balance) =>
      where(balance.playerId === playerId and (
          if (currencyId.isDefined) balance.currencyId === currencyId.get
          else balance.currencyId.isNull))
      select(balance)
    ).head
  }
  
  def join(playerId: UUID, amount: Double, roomId: UUID) {
    val stake = PokerDB.getRoomStake(roomId)
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
      val balance = getBalance(playerId, stake.currencyId)
      val order = orders.insert(Order.buyIn(playerId, amount, roomId))
      purchase(balance, order)
    }
  }
  
  def leave(playerId: UUID, amount: Double, roomId: UUID) {
    val stake = PokerDB.getRoomStake(roomId)
    inTransaction {
      val balance = getBalance(playerId, stake.currencyId)
      val order = orders.insert(Order.leave(playerId, amount, roomId))
      purchase(balance, order)
    }
  }
  
  def register(playerId: UUID, tournamentId: UUID) {
    val buyIn = PokerDB.getTournamentBuyIn(tournamentId)
    val amount = buyIn.price + buyIn.fee
    // TODO check tournament start date, state
    inTransaction {
      val balance = getBalance(playerId, buyIn.currencyId)
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
    val payment = withdraws.insert(Withdraw.create(balance, -amount))
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
    val payment = purchases.insert(Purchase.create(balance, order))
    balance.charge(payment.amount)
  }
  
}
