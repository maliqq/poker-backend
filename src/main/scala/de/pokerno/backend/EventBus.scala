package de.pokerno.backend

import de.pokerno.model._
import de.pokerno.backend.protocol._
import akka.event.{ ActorEventBus, ScanningClassification }
import akka.actor.actorRef2Scala

class EventBus extends ActorEventBus with ScanningClassification {
  type Classifier = String

  trait Route
  case object NoOne extends Route
  case class One(endpoint: Classifier) extends Route
  case object All extends Route
  case class Where[Classifier](f: (Classifier) ⇒ Boolean) extends Route
  case class Only(endpoints: List[Classifier]) extends Route
  case class Except(endpoints: List[Classifier]) extends Route

  case class Notification(message: Message, from: Route = NoOne, to: Route = All)
  type Event = Notification

  def compareClassifiers(a: Classifier, b: Classifier): Int = a compare b

  case class ListOfPlayers(v: List[Player])
  def matches(classifier: Classifier, event: Event): Boolean = event.to match {
    case All             ⇒ true
    case One(id)         ⇒ id == classifier
    case Except(players) ⇒ players.find { id ⇒ id == classifier } == None
    case _               ⇒ throw new Error("unknown route: %s".format(event.to))
  }

  def publish(event: Event, subscriber: Subscriber) = {
    //Console printf ("%s%s -> %s %s%s\n" format (Console.CYAN, event.to, subscriber, event.message, Console.RESET))
    subscriber ! event.message
  }

  def publish(message: Message, route: Route = All) {
    publish(Notification(message, to = route))
  }
}
