package de.pokerno.backend.server

import akka.actor.Actor
import de.pokerno.model
import de.pokerno.gameplay
import concurrent.duration._

trait Presence { a: Actor ⇒

  import context._
  
  def table: model.Table
  def events: gameplay.Events

  case class Away(player: model.Player)

  def playerOffline(player: model.Player) {
    table(player) map { seat =>
      seat.offline()
      seat.actingTimer.pause()
      seat.reconnectTimer.start(new model.AkkaTimers(system.scheduler, system.dispatcher)) {
        self ! Away(seat.player)
      }
      events broadcast gameplay.Events.playerOffline(seat)
    }
  }

  def playerAway(player: model.Player) {
    table(player) map { seat =>
      seat.away()
      seat.actingTimer.done()
      // auto kick
      // TODO: notify away
      //events.publish(gameplay.Events.playerAway(pos, player)) { _.all() }
    }
  }

  def playerOnline(player: model.Player) {
    table(player) map { seat =>
      seat.online()
      seat.actingTimer.resume()
      seat.reconnectTimer.cancel()
      events broadcast gameplay.Events.playerOnline(seat)
    }
  }

}
