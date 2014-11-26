package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.GameEvent
import de.pokerno.hub

trait Destination
object Destination {
  case object All extends Destination
  case class One(player: Player) extends Destination
  case class Except(players: List[Player]) extends Destination
  case class Only(players: List[Player]) extends Destination
}

class Publisher[T <: hub.Exchange[Notification]](roomId: String, _exchange: T)
    extends de.pokerno.hub.Producer[Notification, T] {
  
  def exchange = _exchange

  case class RoutePublisher(publisher: Publisher[T], to: Destination) {
    def publish(evt: GameEvent) {
      publisher.publish(evt, to)
    }
  }
  
  def route(to: Destination)    = RoutePublisher(this, to)
  def one(id: Player)     = route(Destination.One(id))
  def except(id: Player)  = route(Destination.Except(List(id)))
  def only(id: Player)    = route(Destination.One(id))
  
  def publish(evt: GameEvent, to: Destination) {
    publish(Notification(evt, roomId, to))
  }

  def broadcast(evt: GameEvent) {
    publish(Notification(evt, roomId, Destination.All))
  }
}
