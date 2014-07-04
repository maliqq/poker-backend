package de.pokerno.gameplay.tournament

import akka.actor.ActorRef
import de.pokerno.model._
import de.pokerno.model.tournament.Entry

trait Addons {
  
  val ctx: Context
  def tournamentId = ctx.id.toString
  import ctx._
  val payment: de.pokerno.payment.thrift.Payment.FutureIface
  val entries = collection.mutable.Map.empty[Player, Entry]

  def addon(playerId: Player) {
    val f = payment.addon(tournamentId, playerId)
    f.onSuccess { _ =>
      val entry = entries(playerId)
      if (!entry.addon) {
        entry.addon = true
        entry.stack += buyIn.addonStack.get
      }
      metrics.addons.inc()
    }
    
    f.onFailure { case _ =>
    }
  }
  
  def isAddonBreak(): Boolean
  
}
