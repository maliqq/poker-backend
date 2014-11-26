package de.pokerno.form.tournament

import de.pokerno.model._
import de.pokerno.model.tournament._
import de.pokerno.gameplay._
import concurrent.duration._
import java.time.temporal.ChronoUnit

abstract class BuyIn {
  val startingStack: Int
  val addonStack: Option[Int]
}

abstract class Context {
  val id: java.util.UUID
  def tournamentId = id.toString
  
  val variation: Variation
  val balance: de.pokerno.payment.thrift.Payment.FutureIface
  val metrics: Metrics
  val events: Publisher[_]
  val buyIn: BuyIn
  val payment: de.pokerno.payment.thrift.Payment.FutureIface
  val entries = collection.mutable.Map.empty[Player, Entry]
  
  var started: Option[java.time.Instant] = None
  def start() = started = Some(java.time.Instant.now())
  def isStarted = started.isDefined
  
  // start at predefined moment
  val scheduledStart: Option[java.time.Instant] = None
  def isScheduledStart = scheduledStart.isDefined
  
  def isAnnounced: Boolean
  def isCancelled: Boolean
  
  def canJoin: Boolean = canJoinBeforeStart || canLateJoin
  
  def canJoinBeforeStart: Boolean = {
    if (isStarted) return false
    if (isScheduledStart) {
      return !isAnnounced
    }
    true
  }
  
  // late join after start
  val lateJoinPeriod: Option[FiniteDuration] = None
  def withLateJoin = lateJoinPeriod.isDefined
  
  def canLateJoin: Boolean = {
    if (!isStarted || !withLateJoin) return false
    val _started = started.get
    val now = java.time.Instant.now()
    val amount = lateJoinPeriod.get.toMillis
    now.minus(amount, ChronoUnit.MILLIS).isBefore(_started)
  }
  
  var ended: Option[java.time.Instant] = None
  def end() = ended = Some(java.time.Instant.now())
  def isEnded = ended.isDefined
  
  def startingStack = buyIn.startingStack
}
