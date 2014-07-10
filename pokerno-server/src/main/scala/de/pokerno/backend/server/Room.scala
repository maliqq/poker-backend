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

class Room(id: java.util.UUID,
    val variation: model.Variation,
    val stake: model.Stake,
    env: RoomEnv)
    extends de.pokerno.form.CashRoom with Observers {

  import env._
  
  val balance = env.balance
  
  def roomId = id.toString()
  
  val table = new model.Table(variation.tableSize)
  val events = new gameplay.Events(roomId)

  val watchers      = observe(classOf[de.pokerno.form.room.Watchers], f"room-$roomId-watchers")
  val journal       = observe(classOf[de.pokerno.form.room.Journal], f"room-$roomId-journal", "/tmp", roomId)
  val metrics       = observe(classOf[de.pokerno.form.cash.Metrics], f"room-$roomId-metrics", roomId, pokerdb)
  val broadcasting  = observe(classOf[Broadcasting], f"room-$roomId-broadcasts", roomId, broadcasts)
  
  persist.map { notify(_, f"room-$roomId-persist") }
  
  log.info("starting room {}", roomId)
  
  initialize()
  
}
