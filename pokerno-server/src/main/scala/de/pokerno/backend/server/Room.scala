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
    history: Option[ActorRef] = None,
    persist: Option[ActorRef] = None,
    pokerdb: Option[de.pokerno.data.pokerdb.thrift.PokerDB.FutureIface] = None,
    broadcasts: Seq[Broadcast] = Seq()
  )

class Room(val id: java.util.UUID,
    val variation: model.Variation,
    val stake: model.Stake,
    env: RoomEnv)
    extends de.pokerno.form.CashRoom {

  import context._

  val balance = env.balance
  val table = new model.Table(variation.tableSize)

  env.persist.map { ref =>
    gameplayEvents.subscribe(new de.pokerno.hub.impl.ActorConsumer(ref))
  }
  env.history.map { ref =>
    gameplayEvents.subscribe(new de.pokerno.hub.impl.ActorConsumer(ref))
  }

  private val journal       = actorOf(
      Props(classOf[de.pokerno.form.room.Journal], "/tmp", roomId),
      name = f"room-$roomId-journal")
  
  private val metrics       = actorOf(
      Props(classOf[de.pokerno.form.cash.Metrics], roomId, env.pokerdb),
      name = f"room-$roomId-metrics")
  
  private val broadcasting  = actorOf(
      Props(classOf[Broadcasting], roomId, env.broadcasts),
      name = f"room-$roomId-broadcasts")
  
  log.info("starting room {}", roomId)
  
  initialize()
  
}
