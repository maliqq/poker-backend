package de.pokerno.backend.server

import akka.actor.{ Actor, Props, ActorLogging, ActorRef, FSM }
import de.pokerno.model
import de.pokerno.gameplay
import de.pokerno.backend.Gateway
import de.pokerno.backend.gateway.http
import de.pokerno.protocol.{ rpc, cmd, Codec ⇒ codec }
import de.pokerno.protocol.Conversions._
import util.{ Success, Failure }
import scala.concurrent.{ Promise, Future }

object Room {

  object State extends Enumeration {
    type State = Value
    def state(v: String) = new Val(nextId, v)

    val Waiting = state("waiting")
    val Active = state("active")
    val Paused = state("paused")
    val Closed = state("closed")
  }

  case object Close
  case object Pause
  case object Resume

  case class Watch(watcher: http.Connection)
  case class Unwatch(watcher: http.Connection)
  case class Observe(observer: ActorRef, name: String)

}

sealed trait Data
case object NoneRunning extends Data
case class Running(play: gameplay.Play, ref: ActorRef) extends Data

class Room(
  id: String,
  variation: model.Variation,
  stake: model.Stake)
    extends Actor
    with ActorLogging
    with FSM[Room.State.Value, Data] {

  val watchers = collection.mutable.ListBuffer[http.Connection]()
  val table = new model.Table(variation.tableSize)
  val events = new gameplay.Events(id)

  import context._
  import context.dispatcher
  import concurrent.duration._
  import proto.cmd.PlayerEventSchema

  log.info("starting room {}", id)
  startWith(Room.State.Waiting, NoneRunning)

  val observer = actorOf(Props(new Actor {
    def receive = {
      case gameplay.Notification(msg, _, to) ⇒
        import gameplay.Route._
        val data = codec.Json.encode(msg)
        to match {
          // broadcast
          case All ⇒
            watchers.map { _.send(data) }
          // skip
          case Except(ids) ⇒
            watchers.map {
              case w ⇒
                if (w.player.isDefined && !ids.contains(w.player.get))
                  w.send(data)
            }
          // notify one
          case One(id) ⇒
            watchers.find { w ⇒
              w.player.isDefined && w.player.get == id
            }.map { _.send(data) }
        }
    }
  }), name = f"room-$id-observer")

  events.broker.subscribe(observer, f"room-$id-observer")

  when(Room.State.Paused) {
    case Event(Room.Resume, _) ⇒
      goto(Room.State.Active)
  }

  when(Room.State.Waiting) {
    case Event(join: cmd.JoinPlayer, NoneRunning) ⇒
      joinPlayer(join)
      if (canStart) goto(Room.State.Active)
      else stay()
  }

  when(Room.State.Closed) {
    case Event(x: Any, _) ⇒
      log.warning("got {} in closed state", x)
      stay()
  }

  when(Room.State.Active) {
    case Event(Room.Close, Running(_, deal)) ⇒
      context.stop(deal)
      goto(Room.State.Closed)

    case Event(Room.Pause, Running(_, deal)) ⇒
      context.stop(deal)
      goto(Room.State.Paused)

    // first deal in active state
    case Event(gameplay.Deal.Start, NoneRunning) ⇒
      stay() using spawnDeal

     // current deal cancelled
    case Event(gameplay.Deal.Cancel, Running(_, deal)) ⇒
      log.info("deal cancelled")
      goto(Room.State.Waiting) using (NoneRunning)
      
    // current deal stopped
    case Event(gameplay.Deal.Done, Running(_, deal)) ⇒
      val after = nextDealAfter
      log.info("deal done; starting next deal in {}", after)
      self ! gameplay.Deal.Next(after)
      stay() using (NoneRunning)

    // schedule next deal in *after* seconds
    case Event(gameplay.Deal.Next(after), NoneRunning) ⇒
      log.info("starting next deal in {}", after)
      system.scheduler.scheduleOnce(after, self, gameplay.Deal.Start)
      stay()

    // add bet when deal is active
    case Event(addBet: cmd.AddBet, Running(_, deal)) ⇒
      deal ! addBet // pass to deal
      stay()

    case Event(join: cmd.JoinPlayer, _) ⇒
      joinPlayer(join)
      stay()
      
    case Event(chat: cmd.Chat, _) ⇒
      // TODO broadcast
      stay()

    case Event(cmd.PlayerEvent(event, player: String), _) ⇒
      // TODO notify
      event match {
        case PlayerEventSchema.EventType.OFFLINE ⇒
          changeSeatState(player) { _ away }

        case PlayerEventSchema.EventType.SIT_OUT ⇒
          changeSeatState(player) { _ idle }

        case PlayerEventSchema.EventType.COME_BACK | PlayerEventSchema.EventType.ONLINE ⇒
          changeSeatState(player) { _ ready }

        case PlayerEventSchema.EventType.LEAVE ⇒
          table.removePlayer(player)
          changeSeatState(player) { _ clear }
      }
      stay()
  }

  whenUnhandled {
    case Event(Room.Observe(observer, name), _) ⇒
      events.broker.subscribe(observer, name)
      // TODO !!!!!
      //events.start(table, variation, stake, )
      stay()

    case Event(Room.Watch(conn), running) ⇒
      watchers += conn
      //events.broker.subscribe(observer, conn.player.getOrElse(conn.sessionId))
      conn.player map { p ⇒
        changeSeatState(p) { _ ready } // Reconnected
      }
    
      val player = new model.Player(conn.player.getOrElse(conn.sessionId))
      running match {
        case NoneRunning ⇒
          events.start(player, table, variation, stake, null)
          if (canStart) goto(Room.State.Active)
          else stay() 
          
        case Running(play, _) ⇒
          events.start(player, table, variation, stake, play)
          stay()
      }

    case Event(Room.Unwatch(conn), _) ⇒
      watchers -= conn
      //events.broker.unsubscribe(observer, conn.player.getOrElse(conn.sessionId))
      conn.player map { p ⇒
        changeSeatState(p) { _ away }
      }
      stay()

    case Event(kick: cmd.KickPlayer, _) ⇒
      log.info("got kick: {}", kick)
      changeSeatState(kick.player) { _ clear }
      table.removePlayer(kick.player)
      stay()
      
    case Event(x: Any, _) ⇒
      log.warning("unhandled: {}", x)
      stay()
  }

  onTransition {
    case Room.State.Waiting -> Room.State.Active ⇒
      self ! gameplay.Deal.Next(firstDealAfter)
  }

  initialize()

  final val minimumReadyPlayersToStart = 2
  final val firstDealAfter = (10 seconds)
  final val nextDealAfter = (5 seconds)

  private def canStart: Boolean = {
    table.seatsAsList.count(_ isReady) == minimumReadyPlayersToStart
  }

  private def joinPlayer(join: cmd.JoinPlayer) {
    try {
      table.addPlayer(join.pos, join.player, Some(join.amount))
      events.joinTable((join.player, join.pos), join.amount)
    } catch {
      case err: model.Seat.IsTaken        ⇒
      case err: model.Table.AlreadyJoined ⇒
    }
  }

  private def changeSeatState(player: model.Player)(f: model.Seat ⇒ Unit) {
    table.seat(player) map {
      case (seat, pos) ⇒
        f(seat)
        events.seatStateChanged(pos, seat.state)
    }
  }

  private def spawnDeal(): Running = {
    val ctx = new gameplay.Context(table, variation, stake, events)
    val play = new gameplay.Play(ctx)
    val deal = actorOf(Props(classOf[gameplay.Deal], ctx, play))
    Running(play, deal)
  }
}
