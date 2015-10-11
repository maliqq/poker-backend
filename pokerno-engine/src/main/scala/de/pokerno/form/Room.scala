package de.pokerno.form

import akka.actor.{Actor, ActorRef, FSM, ActorLogging}
import de.pokerno.model._
import de.pokerno.gameplay.{Notification, Publisher}
import de.pokerno.hub

object Room {
  object State extends Enumeration {
    type State = Value
    def state(v: String) = new Val(nextId, v)

    val Waiting   = state("waiting")
    val Active    = state("active")
    val Paused    = state("paused")
    val Closed    = state("closed")
  }
  
  trait ChangeState {}
  
  case class Connect(conn: de.pokerno.network.PlayerConnection)
  case class Disconnect(conn: de.pokerno.network.PlayerConnection)

  case object Close extends ChangeState
  case object Pause extends ChangeState
  case object Resume extends ChangeState
  
  case class Created(id: String)
  case class ChangedState(id: String, newState: State.Value)

  // case class Subscribe(name: String)
  // case class Observe(observer: ActorRef, name: String)
  
  case object RoomState

  sealed trait Data
  case object NoneRunning extends Data
  case class Running(ctx: de.pokerno.gameplay.Context, ref: ActorRef) extends Data

  object Topics {
    final val Deals = "room.deals"
    final val State = "room.state"
    final val Metrics = "room.metrics"
  }

  object Metrics {
    case class PlayStatsUpdate(id: String, metrics: cash.Metrics)
    case class PlayersCountUpdate(id: String, metrics: cash.Metrics)
  }
}

class RoomEvents(initial: List[String] = List()) extends hub.TopicExchange[Any] {
  private val _exchange = newExchange
  def exchange = _exchange
  
  private val _topics = collection.mutable.Map[String, hub.impl.Exchange[Any]]()
  def topics = _topics

  def register(name: String) {
    _topics(name) = newExchange
  }

  for (topic <- initial) {
    register(topic)
  }

  private def newExchange = new hub.impl.Exchange[Any]()
}

abstract class Room extends Actor
    with ActorLogging
    with FSM[Room.State.Value, Room.Data]
    with room.JoinLeave {

  protected val roomEvents = buildRoomEvents()
  protected def buildRoomEvents() = new RoomEvents()

  // room attrs
  import Room._
  
  val id: java.util.UUID
  val table: Table
  val variation: Variation
  val stake: Stake
  
  // gameplay events
  protected val gameplayEvents = new hub.impl.Topic[Notification]("room.gameplay.events")
  val events = new Publisher(roomId, gameplayEvents)

  val watchers = new room.Watchers()
  gameplayEvents.subscribe(watchers)

  def roomId: String = id.toString
  protected def canStart: Boolean
  
  /*
   * State machine
   * */
  def paused      = when(State.Paused)_
  def waiting     = when(State.Waiting)_
  def closed      = when(State.Closed)_
  def active      = when(State.Active)_

  def isPaused    = stateName == State.Paused
  def isActive    = stateName == State.Active
  def isWaiting   = stateName == State.Waiting
  def isClosed    = stateName == State.Closed
  
  def toActive()  = goto(State.Active)
  def toClosed()  = goto(State.Closed)
  def toPaused()  = goto(State.Paused)
  def toWaiting() = goto(State.Waiting)
  
  def running: Option[Running] = stateData match {
    case r: Running => Some(r)
    case _ => None
  }
  
  def isRunning = stateData != NoneRunning
  def notRunning = stateData == NoneRunning
  
  def tryResume() = {
    if (notRunning && isWaiting && canStart) toActive()
    else stay()
  }
  
}
