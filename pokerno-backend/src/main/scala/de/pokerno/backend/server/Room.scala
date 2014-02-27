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
    with FSM[Room.State.Value, Data]
    with RoomTimers {
  
  val table = new model.Table(variation.tableSize)
  val events = new gameplay.Events(id)

  import context._
  import context.dispatcher
  import concurrent.duration._
  import proto.cmd.PlayerEventSchema

  log.info("starting room {}", id)
  startWith(Room.State.Waiting, NoneRunning)

  def observe[T <: Actor](actorClass: Class[T], name: String, args: Any*) = {
    val actor = actorOf(Props(actorClass, args:_*), name = name)
    events.broker.subscribe(actor, name)
    actor
  }

  /*
   * Watchers
   * */
  val watchers = observe(classOf[Watchers], f"room-$id-watchers")
  val logger = observe(classOf[Log], f"room-$id-log", "/tmp", id)
  val metrics = observe(classOf[Metrics], f"room-$id-metrics")

  /*
   * State machine
   * */
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
          changeSeatState(player) { _._1 away }
        
        case PlayerEventSchema.EventType.ONLINE =>
          changeSeatState(player) { _._1 ready }

        case PlayerEventSchema.EventType.SIT_OUT ⇒
          changeSeatState(player) { _._1 idle }

        case PlayerEventSchema.EventType.COME_BACK ⇒
          changeSeatState(player) { _._1 ready }

        case PlayerEventSchema.EventType.LEAVE ⇒
          table.removePlayer(player)
          changeSeatState(player, notify = false) { case box @ (seat, pos) =>
            events.publish(gameplay.Events.leaveTable((seat.player.get, pos))) // FIXME unify
            table.clearSeat(pos)
          }
          //changeSeatState(player) { _._1 clear }
      }
      stay()
  }

  whenUnhandled {
    case Event(Room.Observe(observer, name), _) ⇒
      events.broker.subscribe(observer, name)
      // TODO !!!!!
      //events.start(table, variation, stake, )
      stay()

    case Event(w @ Room.Watch(conn), running) ⇒
      // notify seat state change
      conn.player map { p ⇒
        playerReconnected(p)
        changeSeatPresence(p) { _._1 online } // Reconnected
      }
    
      // send start message
      val startMsg = running match {
        case NoneRunning ⇒
          gameplay.Events.start(table, variation, stake, null).msg // TODO: empty play
        case Running(play, _) =>
          gameplay.Events.start(table, variation, stake, play).msg
      }
      conn.send(codec.Json.encode(startMsg))
      
      watchers ! w
    
      // start new deal if needed
      if (running == NoneRunning && canStart) goto(Room.State.Active)
      else stay()
  
    case Event(uw @ Room.Unwatch(conn), _) ⇒
      watchers ! uw
      //events.broker.unsubscribe(observer, conn.player.getOrElse(conn.sessionId))
      conn.player map { p ⇒
        playerDisconnected(p)
        changeSeatPresence(p) { _._1 offline }
      }
      stay()
     
    case Event(PlayerGone(p), _) =>
      playerGone(p)
      changeSeatState(p) { _._1 away }
      stay()

    case Event(kick: cmd.KickPlayer, _) ⇒
      log.info("got kick: {}", kick)
      changeSeatState(kick.player, notify = false) { case box @ (seat, pos) =>
        events.publish(gameplay.Events.leaveTable((seat.player.get, pos))) // FIXME unify
        table.clearSeat(pos)
      }
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
      events.publish(gameplay.Events.joinTable((join.player, join.pos), join.amount))
    } catch {
      case err: model.Seat.IsTaken        ⇒
      case err: model.Table.AlreadyJoined ⇒
    }
  }

  private def changeSeatState(player: model.Player, notify: Boolean = true)(f: ((model.Seat, Int)) ⇒ Unit) {
    table.seat(player) map {
      case box @ (seat, pos) ⇒
        f(box)
        if (notify) events.publish(gameplay.Events.seatStateChanged(pos, seat.state))
    }
  }
  
  private def changeSeatPresence(player: model.Player, notify: Boolean = true)(f: ((model.Seat, Int)) ⇒ Unit) {
    table.seat(player) map {
      case box @ (seat, pos) ⇒
        f(box)
        if (notify) events.publish(gameplay.Events.seatPresenceChanged(pos, seat.presence))
    }
  }

  private def spawnDeal(): Running = {
    val ctx = new gameplay.Context(table, variation, stake, events)
    val play = new gameplay.Play(ctx)
    val deal = actorOf(Props(classOf[gameplay.Deal], ctx, play))
    Running(play, deal)
  }
}
