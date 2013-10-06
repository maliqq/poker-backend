package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.event.{ EventBus }

trait Route
case object NoOne extends Route
case class One[T](endpoint: T) extends Route
case object All extends Route
case class Where[T](f: (T) ⇒ Boolean) extends Route
case class Only[T](endpoints: List[T]) extends Route
case class Except[T](endpoints: List[T]) extends Route

case class Notification(message: Message.Value, from: Route = NoOne, to: Route = All)

class Broadcast extends EventBus {
  def subscribe(subscriber: Subscriber, to: Classifier): Boolean = {
    true
  }

  def unsubscribe(subscriber: Subscriber, from: Classifier): Boolean = {
    true
  }

  def unsubscribe(subscriber: Subscriber) = {}

  def publish(event: Event) = {}

  def publish(event: Notification) {
    Console.printf("!!! [%s] %s\n" format (event.to, event.message))
  }

  def all(message: Message.Value) {
    publish(Notification(message, to = All))
  }

  def one(player: Player)(f: ⇒ Message.Value) {
    publish(Notification(f, to = One(player)))
  }
}
