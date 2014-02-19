package de.pokerno.backend.server

import de.pokerno.protocol.{Codec => codec}
import de.pokerno.gameplay.{Notification, Route}
import de.pokerno.backend.gateway.http
import akka.actor.{Actor, ActorLogging}

class Watchers extends Actor with ActorLogging {
  val watchers = collection.mutable.ListBuffer[http.Connection]()
  
  def receive = {
    case Room.Watch(conn) =>
      watchers += conn
    
    case Room.Unwatch(conn) =>
      watchers -= conn
      
    case Notification(msg, _, to) ⇒
      import Route._
      val data = codec.Json.encode(msg)
      to match {
        // broadcast
        case All ⇒
          watchers.map { _.send(data) }
        // skip
        case Except(ids) ⇒
          watchers.map {
            case w ⇒
              if (w.player.isDefined && !ids.contains(w.player.get))
                w.send(data)
          }
        // notify one
        case One(id) ⇒
          watchers.find { w ⇒
            w.player.isDefined && w.player.get == id
          }.map { _.send(data) }
      }
  }
}
