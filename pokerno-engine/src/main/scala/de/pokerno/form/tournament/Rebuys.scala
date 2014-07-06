package de.pokerno.form.tournament

import de.pokerno.model.tournament.Entry

trait Rebuys {
  
  val ctx: Context
  import ctx._
  
  /*
  Ребаи возможны при нулевом стеке, в перерывах либо по запросу во время текущей раздачи.
  В последнем случае пополнение происходит после окончания текущей раздачи.
  */
  def rebuy(playerId: Player) {
    val f = payment.rebuy(tournamentId, playerId)
    f.onSuccess { _ =>
      val entry = entries(playerId)
      entry.rebuysCount += 1
      entry.stack += buyIn.startingStack
      metrics.rebuys.inc()
    }
    f.onFailure { case _ =>
    }
  }
  
  /*
   Сдвоенные ребаи возможны при нулевом стеке
  */
  def doubleRebuy(playerId: Player) {
    val f = payment.doubleRebuy(tournamentId, playerId)
    f.onSuccess { _ =>
      val entry = entries(playerId)
      entry.rebuysCount += 2
      entry.stack += buyIn.startingStack * 2
      metrics.rebuys.inc(2)
    }
    f.onFailure { case _ =>
    }
  }
  
}
