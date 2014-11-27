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

case class RoomEnv(
    balance: de.pokerno.payment.Service,
    pokerdb: Option[de.pokerno.data.pokerdb.thrift.PokerDB.FutureIface] = None,
    notificationConsumers: Seq[ActorRef] = Seq(),
    topicConsumers: Map[String, ActorRef] = Map.empty()
  )

class Room(val id: java.util.UUID,
    val variation: model.Variation,
    val stake: model.Stake,
    env: RoomEnv)
    extends de.pokerno.form.CashRoom {

  import context._

  val balance = env.balance
  val table = new model.Table(variation.tableSize)

  implicit protected def actor2consumer(ref: ActorRef): de.pokerno.hub.Consumer[gameplay.Notification] = {
    new de.pokerno.hub.impl.ActorConsumer[gameplay.Notification](ref)
  }

  private val _consumers = collection.mutable.ListBuffer[de.pokerno.hub.Consumer[gameplay.Notification]]()
  private val journal       = actorOf(
      Props(classOf[de.pokerno.form.room.Journal], "/tmp", roomId),
      name = f"room-$roomId-journal")
  _consumers += journal
  
  private val metrics       = actorOf(
      Props(classOf[de.pokerno.form.cash.Metrics], roomId, env.pokerdb),
      name = f"room-$roomId-metrics")
  _consumers += metrics

  env.notificationConsumers.map { ref =>
    _consumers += ref
  }

  _consumers.map { consumer =>
    gameplayEvents.subscribe(consumer)
  }

  log.info("starting room {}", roomId)
  
  initialize()
  
}
