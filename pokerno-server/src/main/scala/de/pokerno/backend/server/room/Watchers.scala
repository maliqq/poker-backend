package de.pokerno.backend.server.room

import de.pokerno.gameplay.{Notification, Route}
import de.pokerno.backend.gateway.http
import de.pokerno.backend.gateway.Http.Broadcast
import akka.actor.{ Actor, ActorLogging }
import de.pokerno.protocol.GameEvent

object Watchers {
  case class Watch(watcher: http.Connection)
  case class Unwatch(watcher: http.Connection)

  case class Broadcast(msg: GameEvent)
  case class Send(msg: GameEvent, to: String)
}

// TODO use Netty ChannelGroups

class Watchers extends Actor with ActorLogging with Broadcast {
  import Watchers._
  
  val watchers = collection.mutable.ListBuffer[http.Connection]()
  implicit def connections: Iterable[http.Connection] = watchers

  def receive = {
    case Watch(conn) ⇒
      watchers += conn

    case Unwatch(conn) ⇒
      watchers -= conn

    case Broadcast(msg) ⇒
      broadcast(msg)

    case Send(msg, to) ⇒
      broadcast(msg, to)

    case Notification(msg, _, to) ⇒
      broadcast(msg, to)
  }
}
