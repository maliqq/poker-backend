package de.pokerno.backend.server

import akka.actor.{ Actor, Cancellable }
import de.pokerno.model
import de.pokerno.gameplay
import concurrent.duration._

trait Presence { a: Actor ⇒

  import context._
  
  def table: model.Table
  def events: gameplay.Events

  val awayTimers = collection.mutable.HashMap[model.Player, Cancellable]()
  val autoKickTimers = collection.mutable.HashMap[model.Player, Cancellable]()
  val waitReconnect = (15 seconds)

  case class Away(player: model.Player)

  def playerOffline(player: model.Player) {
    table.playerPos(player) map { pos =>
      val seat = table.seats(pos)
      seat.offline()
      events.publish(gameplay.Events.playerOffline(pos, player)) { _.all() }
    }
    awayTimers(player) = system.scheduler.scheduleOnce(waitReconnect, self, Away(player))
  }

  def playerAway(player: model.Player) {
    table.playerPos(player) map { pos =>
      val seat = table.seats(pos)
      seat.away()
      // TODO: notify away
      //events.publish(gameplay.Events.playerAway(pos, player)) { _.all() }
    }
    awayTimers.remove(player)
  }

  def playerOnline(player: model.Player) {
    table.playerPos(player) map { pos =>
      val seat = table.seats(pos)
      seat.online()
      events.publish(gameplay.Events.playerOnline(pos, player)) { _.all() }
    }
    
    awayTimers.get(player) map(_.cancel())
    awayTimers.remove(player)
  }

}
