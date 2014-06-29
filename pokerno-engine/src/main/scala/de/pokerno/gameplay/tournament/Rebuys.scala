package de.pokerno.gameplay.tournament

import de.pokerno.model.tournament.Entry

trait Rebuys {
  
  val ctx: Context
  import ctx._
  def tournamentId = ctx.id.toString
  val payment: de.pokerno.payment.thrift.Payment.FutureIface
  val entries = collection.mutable.Map.empty[Player, Entry]

  def rebuy(playerId: Player) {
    val f = payment.rebuy(tournamentId, playerId)
    f.onSuccess { _ =>
    }
    f.onFailure { case _ =>
    }
  }
  
  def rebuy_(playerId: Player) {
    val entry = entries(playerId)
    entry.rebuysCount += 1
    entry.stack += buyIn.startingStack
  }
  
  def doubleRebuy(playerId: Player) {
    val f = payment.doubleRebuy(tournamentId, playerId)
    f.onSuccess { _ =>
    }
    f.onFailure { case _ =>
    }
  }
  
}
