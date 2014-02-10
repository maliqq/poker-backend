package de.pokerno.backend.server

import akka.actor.{Actor, Props, ActorLogging, ActorRef, FSM}
import de.pokerno.model
import de.pokerno.gameplay
import de.pokerno.backend.Gateway
import de.pokerno.protocol.rpc
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
  case class Pause
  case object Resume
  
  case class Subscribe(observer: ActorRef, name: String)
  
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

  val table = new model.Table(variation.tableSize)
  val events = new gameplay.Events
  
  import context._
  import context.dispatcher
  import concurrent.duration._
  
  startWith(Room.State.Waiting, NoneRunning)
  
  when(Room.State.Paused) {
    case Event(Room.Resume, _) =>
      goto(Room.State.Active)
  }
  
  when(Room.State.Waiting) {
    case Event(Gateway.Message(gw, join: rpc.JoinPlayer), NoneRunning) =>
      handlePlayerJoin(gw, join)
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
    case Event(Gateway.Message(gw, addBet: rpc.AddBet), Running(deal)) =>
      deal ! addBet // pass to deal
      stay()
      
    case Event(Gateway.Message(gw, msg), _) =>
      msg match {
        case join: rpc.JoinPlayer =>
          handlePlayerJoin(gw, join)
        
        case kick: rpc.KickPlayer =>
          // TODO notify
          table.pos(kick.player) map { pos =>
            table.removePlayer(pos)
          }
        
        case chat: rpc.Chat =>
          // TODO broadcast
          
        case rpc.PlayerEvent(event, player: String) =>
          // TODO notify
          event match {
            case rpc.PlayerEventSchema.EventType.OFFLINE =>
              table.seat(player).map { case (seat, pos) =>
                seat.away()
                events.seatStateChanged(pos, seat.state)
              }
              
            case rpc.PlayerEventSchema.EventType.SIT_OUT =>
              table.seat(player).map { case (seat, pos) =>
                seat.idle()
                events.seatStateChanged(pos, seat.state)
              }
              
            case rpc.PlayerEventSchema.EventType.COME_BACK | rpc.PlayerEventSchema.EventType.ONLINE =>
              table.seat(player).map { case (seat, pos) =>
                seat.ready()
                events.seatStateChanged(pos, seat.state)
              }
              
            case rpc.PlayerEventSchema.EventType.LEAVE =>
              table.seat(player) map { case (seat, pos) =>
                table.removePlayer(pos)
                events.seatStateChanged(pos, seat.state)
              }
          }
      }
      stay()
  }
  
  whenUnhandled {
    case Event(Room.Subscribe(observer, name), _) =>
      events.broker.subscribe(observer, name)
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
  
  def handlePlayerJoin(gw: ActorRef, join: rpc.JoinPlayer) {
    joinPlayer(join).onComplete {
      case Success(box) =>
        events.broker.subscribe(gw, join.player)
        events.joinTable((join.player, join.pos), join.amount)
      case Failure(err) =>
        // ignore
    }
  }
  
  private def joinPlayer(join: rpc.JoinPlayer): Future[Boolean] = {
    val promise = Promise[Boolean]()
    try {
      table.addPlayer(join.pos, join.player, Some(join.amount))
      promise.success(true)
    } catch {
      case err: model.Seat.IsTaken =>
        promise.failure(err)
    }
    promise.future
  }
  
  private def spawnDeal(): ActorRef = {
    val ctx = new gameplay.Context(table, variation, stake, events)
    val deal = actorOf(Props(classOf[gameplay.Deal], ctx), name = "deal-process")
    deal
  }
}
