package de.pokerno.gameplay.tournament

trait Rebuys {
  
  val ctx: Context
  def tournamentId = ctx.id.toString
  val payment: de.pokerno.payment.thrift.Payment.FutureIface
  
  def rebuy(playerId: Player) {
    val f = payment.rebuy(tournamentId, playerId)
    f.onSuccess { _ =>
    }
    f.onFailure { case _ =>
    }
  }
  
  def doubleRebuy(playerId: Player) {
    val f = payment.doubleRebuy(tournamentId, playerId)
    f.onSuccess { _ =>
    }
    f.onFailure { case _ =>
    }
  }
  
}
