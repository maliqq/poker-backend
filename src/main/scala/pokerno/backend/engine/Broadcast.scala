package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.ActorRef
import akka.event.{ ActorEventBus, ScanningClassification }

class Broadcast extends ActorEventBus with ScanningClassification {

  type Classifier = String

  trait Route
  case object NoOne extends Route
  case class One(endpoint: Classifier) extends Route
  case object All extends Route
  case class Where[Classifier](f: (Classifier) ⇒ Boolean) extends Route
  case class Only(endpoints: List[Classifier]) extends Route
  case class Except(endpoints: List[Classifier]) extends Route

  case class Notification(message: Message.Value, from: Route = NoOne, to: Route = All)
  type Event = Notification
  
  def compareClassifiers(a: Classifier, b: Classifier): Int = a compare b
  
  case class ListOfPlayers(v: List[Player])
  def matches(classifier: Classifier, event: Event): Boolean = event.to match {
    case All => true
    case One(id) => id == classifier
    case Except(players) => players.find { id => id == classifier } != None // FIXME
    case _ => throw new Error("unknown route: %s".format(event.to))
  }
  
  def publish(event: Event, subscriber: Subscriber) = {
    Console printf("!!! [%s -> %s] %s\n" format (event.to, subscriber, event.message))
    subscriber ! event.message
  }
  
  def all(message: Message.Value) {
    publish(Notification(message, to = All))
  }

  def one(player: Player)(f: => Message.Value) = {
    publish(Notification(f, to = One(player.id)))
  }
  
  def except(player: Player)(f: => Message.Value) = {
    publish(Notification(f, to = Except(List(player.id))))
  }
  
  def except(players: List[Player])(f: => Message.Value) = {
    publish(Notification(f, to = Except(players.map(_ id))))
  }
  
  def only(players: List[Player])(f: => Message.Value) = {
    publish(Notification(f, to = Only(players.map(_ id))))
  }
}
