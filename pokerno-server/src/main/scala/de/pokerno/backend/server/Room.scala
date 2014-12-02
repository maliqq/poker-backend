package de.pokerno.backend.server

import akka.actor.{ Actor, Props, ActorLogging, ActorRef, FSM }
import de.pokerno.model
import de.pokerno.gameplay
import de.pokerno.backend.Gateway
import de.pokerno.backend.gateway.http
import de.pokerno.protocol.GameEvent
import de.pokerno.protocol.{ cmd, api, msg => message}
import de.pokerno.protocol.thrift
import util.{ Success, Failure }
import scala.concurrent.{ Promise, Future }

import de.pokerno.form.Room.{Topics => RoomTopics}
import de.pokerno.form.Room.{Metrics => RoomMetrics}
import de.pokerno.form.Room.{Created => RoomCreated}

case class RoomEnv(
    balance: de.pokerno.payment.Service,
    notificationConsumers: Seq[ActorRef] = Seq(),
    topicConsumers: Map[String, Seq[ActorRef]] = Map.empty()
  )

class Room(val id: java.util.UUID,
    val variation: model.Variation,
    val stake: model.Stake,
    env: RoomEnv)
    extends de.pokerno.form.CashRoom {

  import context._

  val balance = env.balance
  val table = new model.Table(variation.tableSize)

  implicit protected def actor2consumer[T](ref: ActorRef): de.pokerno.hub.Consumer[T] = {
    new de.pokerno.hub.impl.ActorConsumer[T](ref)
  }

  private val _consumers = collection.mutable.ListBuffer[de.pokerno.hub.Consumer[gameplay.Notification]]()
  private val journal = actorOf(
      Props(classOf[de.pokerno.form.room.Journal], "/tmp", roomId),
      name = f"room-$roomId-journal")
  _consumers += journal
  
  private val metrics = actorOf(
      Props(new de.pokerno.form.cash.MetricsCollector{
        def report() {
          roomEvents.publish(RoomMetrics(roomId, metrics), to = RoomTopics.Metrics)
        }
      }),
      name = f"room-$roomId-metrics")
  _consumers += metrics

  env.notificationConsumers.foreach { ref =>
    _consumers += ref
  }

  _consumers.foreach { consumer =>
    gameplayEvents.subscribe(consumer)
  }

  env.topicConsumers.foreach { case (topic, consumers) =>
    consumers.foreach { consumer =>
      roomEvents.subscribe(consumer, to = topic)
    }
  }

  log.info("starting room {}", roomId)

  initialize()

  roomEvents.publish(RoomCreated(roomId), to = RoomTopics.State)
  
}
