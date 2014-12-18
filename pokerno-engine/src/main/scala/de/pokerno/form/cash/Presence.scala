package de.pokerno.form.cash

import akka.actor.Actor
import de.pokerno.model
import de.pokerno.gameplay
import concurrent.duration._
import de.pokerno.protocol.cmd
import de.pokerno.form.CashRoom

trait Presence extends de.pokerno.form.room.Presence { room: CashRoom ⇒

  import context._

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
      seat.awayTimer.start(new model.AkkaTimers(system.scheduler, system.dispatcher)) {
        self ! cmd.KickPlayer(player)
      }
      // auto kick
      // TODO: notify away
      //events.publish(gameplay.Events.playerAway(pos, player)) { _.all() }
    }
  }

  def sitOut(seat: model.seat.impl.Sitting) {
    seat.idle()
    seat.sitOutTimer.start(new model.AkkaTimers(system.scheduler, system.dispatcher)) {
      self ! cmd.KickPlayer(seat.player)
    }
    events broadcast gameplay.Events.playerSitOut(seat)
  }

  def playerSitOut(player: model.Player, running: Boolean) {
    table(player).map { seat =>
      if (!running) {
        // do sit out immediately
        sitOut(seat)
      } else {
        seat.toggleSittingOut()
      }
    }
  }

  def playerComeBack(player: model.Player) {
    table(player).map { seat =>
      if (seat.isSitOut) {
        seat.ready()
        seat.sitOutTimer.cancel()
        events broadcast gameplay.Events.playerComeBack(seat)
      }
    }
  }

  def playerOnline(player: model.Player) {
    table(player) map { seat =>
      seat.online()
      seat.awayTimer.cancel()
      seat.actingTimer.resume()
      seat.reconnectTimer.cancel()
      events broadcast gameplay.Events.playerOnline(seat)
      if (seat.isTaken) {
        // ask again
        askBuyIn(seat)
      }
    }
  }

}
