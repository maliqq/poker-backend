package de.pokerno.gameplay.tournament

import de.pokerno.model._
import de.pokerno.gameplay._

class BuyIn {
  val startingStack: Int
}

abstract class Context {
  val id: java.util.UUID
  val game: Game
  val balance: de.pokerno.payment.thrift.Payment.FutureIface
  val events: Events
  val buyIn: BuyIn
  
  def startingStack = buyIn.startingStack
}
