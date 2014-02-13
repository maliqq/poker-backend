package de.pokerno.backend.server

import akka.actor.{Actor, Props, ActorLogging, ActorRef, FSM}
import de.pokerno.model
import de.pokerno.gameplay
import de.pokerno.backend.Gateway
import de.pokerno.backend.gateway.http
import de.pokerno.protocol.{rpc, cmd, Codec => codec}
import de.pokerno.protocol.Conversions._
import util.{Success, Failure}
import scala.concurrent.{Promise, Future}

object Room {
  
  object State extends Enumeration {
    type State = Value
    def state(v: String) = new Val(nextId, v)
    
    val Waiting = state("waiting")
    val Active = state("active")
    val Paused = state("paused")
    val Closed = state("closed")
  }
  
  import State._
  type State = Value
  
  case object Close
  case object Pause
  case object Resume
  
  case class Watch(watcher: http.Connection)
  case class Unwatch(watcher: http.Connection)
  case class Observe(observer: ActorRef, name: String)
  
}

sealed trait Data
case object NoneRunning extends Data
case class Running(ref: ActorRef) extends Data

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
  
  log.info("starting node {}", id)
  startWith(Room.State.Waiting, NoneRunning)
  
  val observer = system.actorOf(Props(new Actor {
            def receive = {
              case gameplay.Notification(msg, _, to) =>
                import gameplay.Route._
                val data = codec.Json.encode(msg)
                to match {
                  case All => watchers.map {  _.send(data) }
                  case One(id) => watchers.find { _.player.get == id }.map {  _.send(data) }
                }
            }
          }), name = "room-observer")
  
  when(Room.State.Paused) {
    case Event(Room.Resume, _) =>
      goto(Room.State.Active)
  }
  
  when(Room.State.Waiting) {
    case Event(join: cmd.JoinPlayer, NoneRunning) =>
      joinPlayer(join)
      if (canStart) goto(Room.State.Active)
      else stay()
  }

  when(Room.State.Closed) {
    case Event(x: Any, _) =>
      log.warning("got {} in closed state", x)
      stay()
  }
  
  when(Room.State.Active) {
    case Event(Room.Close, Running(deal)) =>
      context.stop(deal)
      goto(Room.State.Closed)
    
    case Event(Room.Pause, Running(deal)) =>
      context.stop(deal)
      goto(Room.State.Paused)
    
    // first deal in active state
    case Event(gameplay.Deal.Start, NoneRunning) =>
      stay() using Running(spawnDeal)
    
    // previous deal stopped
    case Event(gameplay.Deal.Done, Running(deal)) =>
      val after = nextDealAfter
      log.info("deal done; starting next deal in {}", after)
      self ! gameplay.Deal.Next(after)
      stay() using(NoneRunning)
      
    // schedule next deal in *after* seconds
    case Event(gameplay.Deal.Next(after), NoneRunning) =>
      log.info("starting next deal in {}", after)
      system.scheduler.scheduleOnce(after, self, gameplay.Deal.Start)
      stay()

    // add bet when deal is active
    case Event(addBet: cmd.AddBet, Running(deal)) =>
      deal ! addBet // pass to deal
      stay()
      
    case Event(msg, _) =>
      msg match {
        case join: cmd.JoinPlayer =>
          joinPlayer(join)
        
        case kick: cmd.KickPlayer =>
          // TODO notify
          table.pos(kick.player) map { pos =>
            table.removePlayer(pos)
          }
        
        case chat: cmd.Chat =>
          // TODO broadcast
          
        case cmd.PlayerEvent(event, player: String) =>
          // TODO notify
          event match {
            case PlayerEventSchema.EventType.OFFLINE =>
              table.seat(player).map { case (seat, pos) =>
                seat.away()
                events.seatStateChanged(pos, seat.state)
              }
              
            case PlayerEventSchema.EventType.SIT_OUT =>
              table.seat(player).map { case (seat, pos) =>
                seat.idle()
                events.seatStateChanged(pos, seat.state)
              }
              
            case PlayerEventSchema.EventType.COME_BACK | PlayerEventSchema.EventType.ONLINE =>
              table.seat(player).map { case (seat, pos) =>
                seat.ready()
                events.seatStateChanged(pos, seat.state)
              }
              
            case PlayerEventSchema.EventType.LEAVE =>
              table.seat(player) map { case (seat, pos) =>
                table.removePlayer(pos)
                events.seatStateChanged(pos, seat.state)
              }
          }
      }
      stay()
  }
  
  whenUnhandled {
    case Event(Room.Observe(observer, name), _) =>
      events.broker.subscribe(observer, name)
      // TODO !!!!!
      //events.start(table, variation, stake)
      stay()
    
    case Event(Room.Watch(conn), _) =>
      watchers += conn
      events.broker.subscribe(observer, conn.player.getOrElse(conn.sessionId))
      stay()
      
    case Event(Room.Unwatch(conn), _) =>
      watchers -= conn
      events.broker.unsubscribe(observer, conn.player.getOrElse(conn.sessionId))
      stay()
      
    case Event(x: Any, _) =>
      log.warning("unhandled: {}", x)
      stay()
  }
  
  onTransition {
    case Room.State.Waiting -> Room.State.Active ⇒
      self ! gameplay.Deal.Next(firstDealAfter)
  }
  
  initialize()
  
  final val minimumReadyPlayersToStart = 2
  final val firstDealAfter = (15 seconds)
  final val nextDealAfter = (5 seconds)
  
  private def canStart: Boolean = {
    table.seatsAsList.count(_ isReady) == minimumReadyPlayersToStart
  }
  
  private def joinPlayer(join: cmd.JoinPlayer) {
    try {
      table.addPlayer(join.pos, join.player, Some(join.amount))
      events.joinTable((join.player, join.pos), join.amount)
    } catch {
      case err: model.Seat.IsTaken =>
    }
  }
  
  private def spawnDeal(): ActorRef = {
    val ctx = new gameplay.Context(table, variation, stake, events)
    val deal = actorOf(Props(classOf[gameplay.Deal], ctx), name = "deal-process")
    deal
  }
}
