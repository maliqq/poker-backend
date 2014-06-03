package de.pokerno.backend.server

import akka.actor.{ Actor, Cancellable }
import de.pokerno.model
import de.pokerno.gameplay
import concurrent.duration._

trait Presence { a: Actor ⇒

  import context._

  val presenceTimers = collection.mutable.HashMap[model.Player, Cancellable]()
  val waitReconnect = (15 seconds)

  case class PlayerGone(player: model.Player)

  def playerDisconnected(player: model.Player) {
    presenceTimers(player) = system.scheduler.scheduleOnce(waitReconnect, self, PlayerGone(player))
  }

  def playerReconnected(player: model.Player) {
    presenceTimers.get(player) map(_.cancel())
    presenceTimers.remove(player)
  }

  def playerGone(player: model.Player) {
    presenceTimers.remove(player)
  }

  def table: model.Table
  def events: gameplay.Events

}
