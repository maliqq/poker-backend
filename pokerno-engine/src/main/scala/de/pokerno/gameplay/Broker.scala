package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.msg

import akka.event.{ ActorEventBus, ScanningClassification }

trait Route {}

object Route {
  case object NoOne extends Route
  case object All extends Route
  
  case class One(endpoint: String) extends Route
  
  case class Where(f: (String) ⇒ Boolean) extends Route
  case class Only(endpoints: List[String]) extends Route
  case class Except(endpoints: List[String]) extends Route
}

case class Notification(message: msg.Message, to: Route = Route.All) // from: Route = Route.NoOne, 

private[gameplay] class Broker extends ActorEventBus
    with ScanningClassification {

  type Classifier = String
  type Event = Notification

  def compareClassifiers(a: Classifier, b: Classifier): Int = a compare b

  case class ListOfPlayers(v: List[Player])

  import Route._
  def matches(classifier: Classifier, event: Event): Boolean = event.to match {
    case All             ⇒ true
    case One(id)         ⇒ id == classifier
    case Except(ids)     ⇒ !ids.exists { _ == classifier }
    case Where(f)        => f(classifier)
    case Only(ids)       => ids.contains(classifier)
    case _               ⇒ throw new Error("unknown route: %s".format(event.to))
  }

  def publish(event: Event, subscriber: Subscriber) = {
    subscriber ! event
  }
  
}
