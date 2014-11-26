package de.pokerno.form

import akka.actor.{Actor, ActorRef, FSM, ActorLogging}
import de.pokerno.model._

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
  
  case class ChangedState(id: String, newState: State.Value)

  case class Subscribe(name: String)
  case class Observe(observer: ActorRef, name: String)
  
  case object PlayState

  sealed trait Data
  case object NoneRunning extends Data
  case class Running(ctx: de.pokerno.gameplay.Context, ref: ActorRef) extends Data
}

abstract class Room extends Actor with ActorLogging with FSM[Room.State.Value, Room.Data] with room.JoinLeave {
  import Room._
  
  val id: java.util.UUID
  val table: Table
  val variation: Variation
  val stake: Stake
  val events: de.pokerno.gameplay.Publisher
  
  def roomId: String = id.toString
  protected def canStart: Boolean
  
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
  
  def running: Option[Running] = stateData match {
    case r: Running => Some(r)
    case _ => None
  }
  
  def isRunning = stateData != NoneRunning
  def notRunning = stateData == NoneRunning
  
  def tryResume() = {
    if (notRunning && canStart) toActive()
    else stay()
  }
  
}
