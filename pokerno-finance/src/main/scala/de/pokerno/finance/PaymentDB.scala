package de.pokerno.finance

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.internals.FieldMetaData

object PaymentDB extends Schema {
  
  object Tags {
    final val Transfer  = "transfer"
    final val Deposit   = "deposit"
    final val Withdraw  = "withdraw"
    final val Purchase  = "purchase"
  }
  
  sealed class Currency(var id: Long)
  sealed class Account(var id: Long)
  
  // PAYMENTS
  abstract class Payment(var _type: String) {
    var amount: Double
    var state: String
    var created: java.sql.Timestamp
    var updated: java.sql.Timestamp = null
    def approve() {}
    def reject() {}
  }
  
  // TRANSFERS
  object Transfer {
    def create(from: Balance, to: Balance, amount: Double, state: String) = {
      new Transfer(amount, from.id, to.id, state, java.sql.Timestamp.from(java.time.Instant.now()))
    }
  }
  sealed class Transfer(var amount: Double, var payerId: Long, var payeeId: Long, var state: String, var created: java.sql.Timestamp) extends Payment(Tags.Transfer) {
  }
  
  // DEPOSITS
  object Deposit {
    def create(balance: Balance, amount: Double, state: String) = {
      new Deposit(amount, balance.id, state, java.sql.Timestamp.from(java.time.Instant.now()))
    }
  }
  sealed class Deposit(var amount: Double, var payeeId: Long, var state: String, var created: java.sql.Timestamp) extends Payment(Tags.Deposit) {
  }
  
  // WITHDRAWS
  object Withdraw {
    def create(balance: Balance, amount: Double, state: String) = {
      new Withdraw(amount, balance.id, state, java.sql.Timestamp.from(java.time.Instant.now()))
    }
  }
  sealed class Withdraw(var amount: Double, var payerId: Long, var state: String, var created: java.sql.Timestamp) extends Payment(Tags.Withdraw) {
  }
  
  // PURCHASES
  object Purchase {
    def create(balance: Balance, order: Order, state: String) = {
      new Purchase(order.amount, balance.id, order.id, state, java.sql.Timestamp.from(java.time.Instant.now()))
    }
  }
  sealed class Purchase(var amount: Double, var payerId: Long, var orderId: Long, var state: String, var created: java.sql.Timestamp) extends Payment(Tags.Purchase) {
  }
  
  sealed class Order(var id: Long, var amount: Double, var state: String, var itemId: String, var itemType: String, var itemMetaData: String) {
    def pay() {
    }
  }
  
  sealed class Balance(
      var id: Long,
      var currencyId: Long,
      var amount: Double
  ) {
    
    def charge(diff: Double) {
      update(balances)((balance) =>
        where(balance.id === this.id)
        set(balance.amount := balance.amount + diff)
      )
    }
    
  }

  val balances = table[Balance]("balances")
  
  def withdraw(balance: Balance, amount: Double) = inTransaction {
    balance.charge(-amount) // block amount for withdraw
    val payment = Withdraw.create(balance, amount = amount, state = "pending")
  }
  
  def cancelWithdraw(payment: Withdraw) = inTransaction {
    val balance = balances.where((balance) => balance.id === payment.payerId).head
    balance.charge(payment.amount)
    payment.reject() // rolback if state != "pending"
  }
  
  def deposit(balance: Balance, amount: Double) = inTransaction {
    //val payment = Deposit.create(balance, amount = amount, state = "approved")
    balance.charge(amount)
  }
  
  def transfer(from: Balance, to: Balance, amount: Double) = inTransaction {
    from.charge(-amount)
    to.charge(amount)
    //val payment = Transfer.create(from, to, amount = amount, state = "pending")
  }
  
  def purchase(balance: Balance, order: Order) = inTransaction {
    balance.charge(order.amount)
    val payment = Purchase.create(balance, order, "pending")
    order.pay() // rollback if state != "pending"
    payment.approve()
  }
  
}
