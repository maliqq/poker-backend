package de.pokerno.backend.server

import akka.actor.{ Actor, ActorRef, ActorSystem, Cancellable }
import de.pokerno.model
import de.pokerno.gameplay
import de.pokerno.protocol.cmd
import concurrent.duration._

abstract class Scheduler {
  def schedule(duration: FiniteDuration, msg: Any): Cancellable
}

class Timers(scheduler: Scheduler) {
  val players = collection.mutable.HashMap[model.Player, Cancellable]()
  
  def schedule(player: model.Player, duration: FiniteDuration, msg: Any) {
    players(player) = scheduler.schedule(duration, msg)
  }
  
  def cancel(player: model.Player) {
    players.get(player).map(_.cancel())
    players.remove(player)
  }
}

trait Presence { a: Actor ⇒

  import context._
  
  def table: model.Table
  def events: gameplay.Events

  val _scheduler = new Scheduler {
    def schedule(duration: FiniteDuration, msg: Any) =
      system.scheduler.scheduleOnce(duration, self, msg)
  }

  private val awayTimers = new Timers(_scheduler)
  private val autoKickTimers = new Timers(_scheduler)
  private val waitReconnect = (15 seconds)

  case class Away(player: model.Player)

  def playerOffline(player: model.Player) {
    table.playerSeat(player) map { seat =>
      seat.offline()
      events broadcast gameplay.Events.playerOffline(seat)
    }
    
    awayTimers.schedule(player, waitReconnect, Away(player))
  }

  def playerAway(player: model.Player) {
    table.playerSeat(player) map { seat =>
      seat.away()
      autoKickTimers.schedule(player, waitReconnect, cmd.KickPlayer(player))
      // TODO: notify away
      //events.publish(gameplay.Events.playerAway(pos, player)) { _.all() }
    }
    
    awayTimers.cancel(player)
  }

  def playerOnline(player: model.Player) {
    table.playerSeat(player) map { seat =>
      seat.online()
      events broadcast gameplay.Events.playerOnline(seat)
    }
    
    awayTimers.cancel(player)
    autoKickTimers.cancel(player)
  }

}
