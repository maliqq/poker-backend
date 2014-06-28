package de.pokerno.payment

import org.squeryl.PrimitiveTypeMode._

import model._
import de.pokerno.data.pokerdb

/*
Участие в турнирах
*/
object Tournament {

  import PaymentDB._

  def register(tournamentId: UUID, playerId: UUID) {
    val buyIn = pokerdb.model.Tournament.getBuyIn(tournamentId)
    val amount = buyIn.price + buyIn.fee
    // TODO check tournament start date, state
    inTransaction {
      val balance = Balance.get(playerId, buyIn.currencyId)
      val order = orders.insert(Order.register(playerId, amount, tournamentId))
      purchase(balance, order)
    }
  }
  
}
