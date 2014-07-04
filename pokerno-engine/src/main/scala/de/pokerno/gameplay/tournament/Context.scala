package de.pokerno.gameplay.tournament

import de.pokerno.model._
import de.pokerno.model.tournament._
import de.pokerno.gameplay._

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
  val events: Events
  val broker: Broker
  val buyIn: BuyIn
  val payment: de.pokerno.payment.thrift.Payment.FutureIface
  val entries = collection.mutable.Map.empty[Player, Entry]
  
  def startingStack = buyIn.startingStack
}
