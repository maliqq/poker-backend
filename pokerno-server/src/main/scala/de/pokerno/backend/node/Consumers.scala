package de.pokerno.backend.node

import akka.actor.{Actor, ActorSystem, ActorRef, Props}
import de.pokerno.backend.handler

trait Consumers { a: Actor =>
  import context._
  import de.pokerno.form.Room
  import Room.Topics
  import de.pokerno.backend.server.Node.Metrics

  def nodeId: java.util.UUID
  def syncClient: de.pokerno.client.HttpClient

  protected val notificationConsumers = collection.mutable.ListBuffer[ActorRef]()
  def subscribeNotifications(consumer: ActorRef) {
    notificationConsumers += consumer
  }

  protected val topicConsumers = collection.mutable.Map[String, List[ActorRef]]().withDefaultValue(Nil)
  def subscribeTopic(topic: String, consumer: ActorRef) {
    topicConsumers(Topics.Deals) :+= consumer
  }

  // sync handler
  private var _metrics: MetricsHandler = _
  lazy val metrics = _metrics

  {
    val consumer = actorOf(Props(classOf[handler.SyncHandler], syncClient))
    subscribeNotifications(consumer)
    subscribeTopic(Topics.State, consumer)
    subscribeTopic(Topics.Metrics, consumer)

    // node metrics
    _metrics = new MetricsHandler {
      def report() {
        consumer ! Metrics(nodeId.toString(), this.metrics)
      }
    }
  }

  // history handler
  {
    val consumer = actorOf(Props(classOf[handler.HistoryHandler], syncClient))
    subscribeTopic(Topics.Deals, consumer)
  }

  // broadcast handler
  {
    val consumer = actorOf(Props(classOf[handler.BroadcastHandler]))
    subscribeTopic(Topics.State, consumer)
    subscribeTopic(Topics.Metrics, consumer)
  }
}
