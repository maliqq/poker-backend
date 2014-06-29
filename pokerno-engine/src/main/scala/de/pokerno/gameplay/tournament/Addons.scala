package de.pokerno.gameplay.tournament

import akka.actor.ActorRef
import de.pokerno.model._

trait Addons {
  
  val ctx: Context
  def tournamentId = ctx.id.toString
  val payment: de.pokerno.payment.thrift.Payment.FutureIface
  
  def addon(playerId: Player) {
    val f = payment.addon(tournamentId, playerId)
    f.onSuccess { _ =>
    }
    f.onFailure { case _ =>
    }
  }
  
  def isAddonBreak(): Boolean
  
}
