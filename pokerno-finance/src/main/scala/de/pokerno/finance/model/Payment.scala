package de.pokerno.finance.model

import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

object PaymentType {
  final val Transfer  = "Payment::Transfer"
  final val Deposit   = "Payment::Deposit"
  final val Withdraw  = "Payment::Withdraw"
  final val Purchase  = "Payment::Purchase"
  final val Award = "Payment::Award"
}

object PaymentState {
  // TODO
  final val Pending = "pending"
  final val Rejected = "rejected"
  final val Approved = "approved"
  final val Cancelled = "cancelled"
  final val Processing = "processing"
  final val Failed = "failed"
}

abstract class Payment(var `type`: String)  extends KeyedEntity[Long] {
  var id: Long = 0
  var amount: Double
  var state: String
  @Column("created_at") var created: java.sql.Timestamp
  @Column("updated_at") var updated: java.sql.Timestamp = null
}