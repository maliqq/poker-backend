package de.pokerno.backend.server

import de.pokerno.gameplay.{ Notification, Route }
import de.pokerno.backend.gateway.http
import akka.actor.{ Actor, ActorLogging }
import de.pokerno.protocol.{GameEvent, PlayerEvent}

object Watchers {
  case class Broadcast(msg: GameEvent)
  case class Send(to: String, msg: GameEvent)
}

class Watchers extends Actor with ActorLogging {
  val watchers = collection.mutable.ListBuffer[http.Connection]()

  def receive = {
    case Room.Watch(conn) ⇒
      watchers += conn

    case Room.Unwatch(conn) ⇒
      watchers -= conn

    case Watchers.Broadcast(msg) ⇒
      val data = GameEvent.encode(msg)
      watchers.map { _.send(data) }

    case Watchers.Send(to, msg) ⇒
      val data = GameEvent.encode(msg)
      watchers.find { _.sessionId == to } map { _.send(data) }

    case Notification(msg, _, to) ⇒
      import Route._
      val data = GameEvent.encode(msg)
      watchers.map { w ⇒
        if (to match {
          case All ⇒ true // broadcast

          case Except(ids) ⇒ // skip
            w.player.isDefined && !ids.contains(w.player.get)

          case One(id) ⇒ // notify one
            w.player.isDefined && w.player.get == id

          case _ ⇒ false
        }) w.send(data)
      }
  }
}
