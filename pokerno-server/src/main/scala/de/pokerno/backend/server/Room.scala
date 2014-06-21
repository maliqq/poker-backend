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
import de.pokerno.backend.server.room._

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
  
  case class ChangedState(id: String, newState: State.Value)

  case class Observe(observer: ActorRef, name: String)
  
  case object PlayState
  
}

sealed trait Data
case object NoneRunning extends Data
case class Running(ctx: gameplay.Context, ref: ActorRef) extends Data

class Room(
  val id: java.util.UUID,
  variation: model.Variation,
  val stake: model.Stake,
  val balance: de.pokerno.finance.Service,
  val persist: ActorRef,
  val history: ActorRef,
  pokerdb: Option[de.pokerno.data.pokerdb.thrift.PokerDB.FutureIface],
  broadcasts: Seq[Broadcast])
    extends Actor
    with ActorLogging
    with FSM[Room.State.Value, Data]
    with JoinLeave
    with Presence
    with Observers
    //with Balance
    with gameplay.DealCycle {
  
  def roomId = id.toString()
  
  val table = new model.Table(variation.tableSize)
  val events = new gameplay.Events(roomId)

  import context._
  import context.dispatcher
  import concurrent.duration._
  import Room._

  val watchers      = observe(classOf[Watchers],
                    f"room-$roomId-watchers")
  val journal       = observe(classOf[Journal],
                    f"room-$roomId-journal", "/tmp", roomId)
  val metrics       = observe(classOf[Metrics],
                    f"room-$roomId-metrics", roomId, pokerdb)
  val broadcasting  = observe(classOf[Broadcasting],
                    f"room-$roomId-broadcasts", roomId, broadcasts)
  notify(persist, f"room-$roomId-persist")
  notify(history, f"room-$roomId-history")
  
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
    case Event(gameplay.Deal.Done, Running(ctx, deal)) ⇒
      log.info("deal complete")
      history ! (id, ctx.game, ctx.stake, ctx.play)
      val after = nextDealAfter
      self ! gameplay.Deal.Next(after)
      stay() using (NoneRunning)

    // schedule next deal in *after* seconds
    case Event(gameplay.Deal.Next(after), NoneRunning) ⇒
      log.info("next deal will start in {}", after)
      system.scheduler.scheduleOnce(after, self, gameplay.Deal.Start)
      stay()

    // add bet
    case Event(addBet: cmd.AddBet, Running(_, deal)) ⇒
      deal ! gameplay.Betting.Add(addBet.player, addBet.bet) // pass to deal
      stay()

    // discard cards
    case Event(discard: cmd.DiscardCards, Running(_, deal)) ⇒
      deal ! gameplay.Discarding.Discard(discard.player, discard.cards) // pass to deal
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

    case Event(Connect(conn), current) ⇒
      // notify seat state change
      conn.player map (playerOnline(_))

      // send start message
      val startMsg: GameEvent = current match {
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
   
    case Event(cmd.ComeBack(player), _) =>
      table.playerSeat(player).map { seat =>
        // we're ready
        seat.ready()
        events broadcast gameplay.Events.playerComeBack(seat)
      }
      runOrStay()
      
    case Event(cmd.SitOut(player), current) =>
      table.playerSeat(player).map { seat =>
        current match {
          case NoneRunning =>
            // do sit out immediately
            seat.idle()
            events broadcast gameplay.Events.playerSitOut(seat)
            
          case _ =>             seat.toggleSitOut()
        }
      }
      
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
      persist ! Room.ChangedState(roomId, State.Active)
    case State.Active -> State.Waiting =>
      persist ! Room.ChangedState(roomId, State.Waiting)
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
    val ctx = new gameplay.Context(roomId, table, variation, stake, balance, events)
    val deal = actorOf(Props(classOf[gameplay.Deal], ctx), name = f"room-$roomId-deal-${ctx.play.id}")
    Running(ctx, deal)
  }
}
