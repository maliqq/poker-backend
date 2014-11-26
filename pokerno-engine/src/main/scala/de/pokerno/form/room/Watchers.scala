package de.pokerno.form.room

import de.pokerno.gameplay.{Notification, Destination}
import de.pokerno.protocol.GameEvent
import de.pokerno.hub._

case class Watcher(conn: de.pokerno.network.PlayerConnection) extends impl.NetworkConsumer[GameEvent](conn) {
  def routeKey = conn.player
  override def consume(msg: GameEvent) {
    conn.send(GameEvent.encode(msg))
  }
}

private trait watcherToRouteKey {
  def consumer2routeKey(watcher: Consumer[GameEvent]): Option[String] = {
    watcher.asInstanceOf[Watcher].routeKey
  }
}

// TODO use Netty ChannelGroups
trait Broadcast[T <: Exchange[GameEvent]]
    extends de.pokerno.network.Broadcast[GameEvent.type, GameEvent]
    with RouteDispatcher[GameEvent, T]
    with Consumer[Notification] {
  
  implicit val codec: GameEvent.type = GameEvent
  implicit def connections: Iterable[Watcher] = exchange.consumers.map { consumer => consumer.asInstanceOf[Watcher] }
  
  def consume(e: Notification) {
    val route = e.to match {
      case Destination.All =>
        Route.All
        // TODO: fanout()
      
      case Destination.Except(ids) =>
        new Route.Except[GameEvent, String](ids) with watcherToRouteKey
      
      case Destination.One(id) =>
        new Route.One[GameEvent, String](id) with watcherToRouteKey
        // TODO: direct()
    }
    publish(e.payload, route)
  }
  
}

class Watchers extends Broadcast[impl.Exchange[GameEvent]] {
  val exchange = new impl.Exchange[GameEvent]()

  def subscribe(conn: de.pokerno.network.PlayerConnection) {
    exchange.subscribe(Watcher(conn))
  }

  def unsubscribe(conn: de.pokerno.network.PlayerConnection) {
    exchange.unsubscribe(Watcher(conn))
  }
}
