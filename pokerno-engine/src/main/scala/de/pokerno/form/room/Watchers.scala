package de.pokerno.form.room

import de.pokerno.gameplay.{Event, Destination}
import akka.actor.{ Actor, ActorLogging }
import de.pokerno.protocol.GameEvent
import de.pokerno.hub._

class Watcher extends de.pokerno.network.PlayerConnection with de.pokerno.hub.Consumer[GameEvent] {
  implicit val codec: GameEvent.type = GameEvent
  
  def consume(msg: GameEvent) {
    send(codec.encode(msg))
  }
}

trait WatcherToRouteKey {
  def consumer2routeKey(watcher: Watcher): Option[String] = {
    watcher.player
  }
}

// TODO use Netty ChannelGroups
trait Broadcast extends de.pokerno.network.Broadcast[GameEvent.type, Watcher] with de.pokerno.hub.RouteDispatcher[GameEvent] {
  implicit val codec: GameEvent.type = GameEvent
  implicit def connections: Iterable[Watcher] = exchange.consumers.map { consumer => consumer.asInstanceOf[Watcher] }
  
  def broadcast(e: Event) {
    val route = e.to match {
      case Destination.All =>
        Route.All
      case Destination.Except(ids) =>
        new Route.Except[Watcher, String](ids) with WatcherToRouteKey
      case Destination.One(id) =>
        new Route.One[Watcher, String](id) with WatcherToRouteKey
    }
    publish(e.payload, route)
  }
  
}

class Watchers extends Broadcast
