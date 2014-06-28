package de.pokerno.payment

import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._

import model._
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
  
  def cancelWithdraw(payment: Withdraw) = inTransaction {
    val balance = balances.where((balance) => balance.id === payment.payerId).head
    balance.charge(payment.amount)
    //payment.reject() // rollback if state != "pending"
  }
  
  def withdraw(balance: Balance, amount: Double) = {
    val payment = Withdraw.create(balance, -amount)
    balance.charge(payment.amount) // block amount for withdraw
  }

  def deposit(balance: Balance, amount: Double) = {
    //val payment = Deposit.create(balance, amount = amount, state = "approved")
    balance.charge(amount)
  }
  
  def transfer(from: Balance, to: Balance, amount: Double) = {
    from.charge(-amount)
    to.charge(amount)
    //val payment = Transfer.create(from, to, amount = amount, state = "pending")
  }
  
  def purchase(balance: Balance, order: Order) = {
    val payment = Purchase.create(balance, order)
    balance.charge(payment.amount)
  }
  
  def award(balance: Balance, order: Order) = {
    val payment = Award.create(balance, order)
    balance.charge(payment.amount)
  }
  
}
