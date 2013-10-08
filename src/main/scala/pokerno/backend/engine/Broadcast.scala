package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.ActorRef
import akka.event.{ ActorEventBus, ScanningClassification }

trait Route
case object NoOne extends Route
case class One[T](endpoint: T) extends Route
case object All extends Route
case class Where[T](f: (T) ⇒ Boolean) extends Route
case class Only[T](endpoints: List[T]) extends Route
case class Except[T](endpoints: List[T]) extends Route

case class Notification(message: Message.Value, from: Route = NoOne, to: Route = All)

class Broadcast extends ActorEventBus with ScanningClassification {
  type Event = Notification
  type Classifier = String
  
  def compareClassifiers(a: Classifier, b: Classifier): Int = a compare b
  
  def matches(classifier: Classifier, event: Event): Boolean = event.to match {
    case All => true
    case One(id) => id == classifier
    case _ => throw new Error("unknown route: %s".format(event.to))
  }
  
  def publish(event: Event, subscriber: Subscriber) = {
    Console printf("!!! [%s -> %s] %s\n" format (event.to, subscriber, event.message))
    subscriber ! event.message
  }
  
  def all(message: Message.Value) {
    publish(Notification(message, to = All))
  }

  def one(player: Player)(f: ⇒ Message.Value) {
    publish(Notification(f, to = One(player)))
  }
}
