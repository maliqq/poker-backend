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

object Room {

  object State extends Enumeration {
    type State = Value
    def state(v: String) = new Val(nextId, v)

    val Waiting   = state("waiting")
    val Active    = state("active")
    val Paused    = state("paused")
    val Closed    = state("closed")
  }
  
  case class Connect(conn: http.Connection)
  case class Disconnect(conn: http.Connection)
  
  trait ChangeState

  case object Close extends ChangeState
  case object Pause extends ChangeState
  case object Resume extends ChangeState

  case class Observe(observer: ActorRef, name: String)
  
  case object PlayState
  
}

sealed trait Data
case object NoneRunning extends Data
case class Running(ctx: gameplay.Context, ref: ActorRef) extends Data

class Room(
  val roomId: String,
  variation: model.Variation,
  stake: model.Stake)
    extends Actor
    with ActorLogging
    with FSM[Room.State.Value, Data]
    with JoinLeave
    with Presence
    with Observers
    with gameplay.DealCycle {
  
  val table = new model.Table(variation.tableSize)
  val events = new gameplay.Events(roomId)

  import context._
  import context.dispatcher
  import concurrent.duration._
  import Room._

  val watchers  = observe(classOf[Watchers], f"room-$roomId-watchers")
  val logger    = observe(classOf[Journal], f"room-$roomId-journal", "/tmp", roomId)
  val metrics   = observe(classOf[Metrics], f"room-$roomId-metrics", roomId)

  log.info("starting room {}", roomId)
  startWith(State.Waiting, NoneRunning)

  /*
   * State machine
   * */
  def paused      = when(State.Paused)_
  def waiting     = when(State.Waiting)_
  def closed      = when(State.Closed)_
  def active      = when(State.Active)_
  
  def toActive()  = goto(State.Active)
  def toClosed()  = goto(State.Closed)
  def toPaused()  = goto(State.Paused)
  def toWaiting() = goto(State.Waiting)
  
  paused {
    case Event(Resume, _) ⇒ toActive()
  }
  
  waiting {
    case Event(join: cmd.JoinPlayer, NoneRunning) ⇒
      joinPlayer(join)
      runOrStay()
  }
  
  closed {
    case Event(x: Any, _) ⇒
      log.warning("got {} in closed state", x)
      stay()
  }
  
  active {
    case Event(Close, Running(_, deal)) ⇒
      context.stop(deal)
      toClosed()

    case Event(Pause, Running(_, deal)) ⇒
      context.stop(deal)
      toPaused()

    // first deal in active state
    case Event(gameplay.Deal.Start, NoneRunning) ⇒
      log.info("deal start")
      stay() using startDeal()

    // current deal cancelled
    case Event(gameplay.Deal.Cancel, Running(_, deal)) ⇒
      log.info("deal cancelled")
      toWaiting() using (NoneRunning)

    // current deal stopped
    case Event(gameplay.Deal.Done, Running(_, deal)) ⇒
      log.info("deal complete")
      val after = nextDealAfter
      self ! gameplay.Deal.Next(after)
      stay() using (NoneRunning)

    // schedule next deal in *after* seconds
    case Event(gameplay.Deal.Next(after), NoneRunning) ⇒
      log.info("next deal will start in {}", after)
      system.scheduler.scheduleOnce(after, self, gameplay.Deal.Start)
      stay()

    // add bet when deal is active
    case Event(addBet: cmd.AddBet, Running(_, deal)) ⇒
      deal ! gameplay.Betting.Add(addBet.player, addBet.bet) // pass to deal
      stay()

    case Event(join: cmd.JoinPlayer, _) ⇒
      joinPlayer(join)
      stay()

    case Event(chat: cmd.Chat, _) ⇒
      // TODO broadcast
      stay()
    
    case Event(cmd.AdvanceStack(player, amount), _) =>
      table.playerSeat(player).map { seat =>
        seat.buyIn(amount)
      }
      runOrStay()
      
    case Event(cmd.ChangePlayerState(player, newState), _) =>
      stay()
//    case Event(sitout: cmd.SitOut, _) =>
//      stay()
//    case Event(comeback: cmd.ComeBack, _) =>
//      stay()
      
//    case Event(cmd.PlayerEvent(event, player: String), _) ⇒
//      // TODO notify
//      event match {
//        case PlayerEventSchema.EventType.OFFLINE ⇒
//          changeSeatState(player) { _._1 away }
//
//        case PlayerEventSchema.EventType.ONLINE ⇒
//          changeSeatState(player) { _._1 ready }
//
//        case PlayerEventSchema.EventType.SIT_OUT ⇒
//          changeSeatState(player) { _._1 idle }
//
//        case PlayerEventSchema.EventType.COME_BACK ⇒
//          changeSeatState(player) { _._1 ready }
//
//        case PlayerEventSchema.EventType.LEAVE ⇒
//          leavePlayer(player)
//      }
//      stay()
  }

  whenUnhandled {
    case Event(Room.Observe(observer, name), _) ⇒
      events.broker.subscribe(observer, name)
      // TODO !!!!!
      //events.start(table, variation, stake, )
      stay()

    case Event(Connect(conn), running) ⇒
      // notify seat state change
      conn.player map (playerOnline(_))

      // send start message
      val startMsg: GameEvent = running match {
        case NoneRunning ⇒
          gameplay.Events.start(roomId, table, variation, stake) // TODO: empty play
        case Running(ctx, deal) ⇒
          gameplay.Events.start(ctx, conn.player)
      }
    
      conn.send(GameEvent.encode(startMsg))

      watchers ! Watchers.Watch(conn)

      // start new deal if needed
      runOrStay()

    case Event(Disconnect(conn), _) ⇒
      watchers ! Watchers.Unwatch(conn)
      //events.broker.unsubscribe(observer, conn.player.getOrElse(conn.sessionId))
      conn.player map (playerOffline(_))
      stay()

    case Event(Away(player), _) ⇒
      playerAway(player)
      stay()

    case Event(kick: cmd.KickPlayer, _) ⇒
      leavePlayer(kick.player)
      stay()
   
    case Event(PlayState, NoneRunning) =>
      sender ! api.PlayState(roomId, table, variation, stake)
      stay()
      
    case Event(PlayState, Running(ctx, _)) =>
      sender ! api.PlayState(ctx)
      stay()
      
    case Event(x: Any, _) ⇒
      log.warning("unhandled: {}", x)
      stay()
  }

  onTransition {
    case State.Waiting -> State.Active ⇒
      self ! gameplay.Deal.Next(firstDealAfter)
  }
  
  initialize()
  
  def running: Option[Running] = stateData match {
    case r: Running => Some(r)
    case _ => None
  }
  
  def isRunning = stateData != NoneRunning
  def notRunning = stateData == NoneRunning
  
  def runOrStay() = {
    if (notRunning && canStart) toActive()
    else stay()
  }

  private def startDeal(): Running = {
    val ctx = new gameplay.Context(roomId, table, variation, stake, events)
    val deal = actorOf(Props(classOf[gameplay.Deal], ctx), name = f"room-$roomId-deal-${ctx.play.id}")
    Running(ctx, deal)
  }
}
