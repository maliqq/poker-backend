package de.pokerno.form.room

import de.pokerno.gameplay.{Notification, Route}
import akka.actor.{ Actor, ActorLogging }
import de.pokerno.protocol.GameEvent

object Watchers {
  case class Watch(watcher: de.pokerno.network.PlayerConnection)
  case class Unwatch(watcher: de.pokerno.network.PlayerConnection)

  case class Broadcast(msg: GameEvent)
  case class Send(msg: GameEvent, to: String)
}

trait Broadcast[T <: de.pokerno.network.Codec] extends de.pokerno.network.Broadcast[T] {
  
  def broadcast(msg: Any, to: Route)(implicit connections: Iterable[de.pokerno.network.PlayerConnection]) {
    val data = GameEvent.encode(msg)
    connections.map { conn ⇒
      if (to match {
        case Route.All ⇒ true // broadcast

        case Route.Except(ids) ⇒ // skip
          conn.hasPlayer && !ids.contains(conn.player.get)

        case Route.One(id) ⇒ // notify one
          conn.hasPlayer && conn.player.get == id

        case _ ⇒ false
      }) conn.send(data)
    }
  }
  
}

// TODO use Netty ChannelGroups

class Watchers extends Actor with ActorLogging with Broadcast[GameEvent.type] {
  import Watchers._
  
  val watchers = collection.mutable.ListBuffer[de.pokerno.network.PlayerConnection]()
  implicit def connections: Iterable[de.pokerno.network.PlayerConnection] = watchers
  implicit val codec: GameEvent.type = GameEvent

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
