package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.GameEvent

import akka.event.{ ActorEventBus, EventBus, ScanningClassification }
//import akka.util.Index
//import java.util.concurrent.ConcurrentSkipListSet
//import java.util.Comparator

trait Route {}

object Route {
  case object NoOne extends Route
  case object All extends Route

  case class One(endpoint: String) extends Route

  case class Where(f: (String) ⇒ Boolean) extends Route
  case class Only(endpoints: List[String]) extends Route
  case class Except(endpoints: List[String]) extends Route
}

case class Notification(message: GameEvent, from: Route = Route.NoOne, to: Route = Route.All) // from: Route = Route.NoOne, 

class Broker(id: String) extends ActorEventBus
    with ScanningClassification {

  type Classifier = String
  type Event = Notification

  def compareClassifiers(a: Classifier, b: Classifier): Int = a compare b

  import Route._
  def matches(classifier: Classifier, event: Event) = true

  def publish(event: Event, subscriber: Subscriber) = {
    subscriber ! event.copy(from = One(id))
  }

}
