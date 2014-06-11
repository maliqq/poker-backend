package de.pokerno.model.seat

import de.pokerno.model.Seat
import math.{BigDecimal => Decimal}

trait Transitions {
  class States[T](initial: T) {
    private val transitions = collection.mutable.HashMap[T, T]()
  }
}

trait States {

  import Seat.State
  
  def state: State.Value
  def state_=(v: State.Value)
  def total: Decimal
  
  def play(): Unit =
    state = State.Play

  def idle(): Unit =
    state = State.Idle

  def ready(): Unit =
    state = if (total == 0) State.Idle else State.Ready

  def away(): Unit =
    state = State.Away
  
  def isEmpty =
    state == State.Empty

  def isTaken =
    state == State.Taken

  def isReady =
    state == State.Ready
  
  def isAway =
    state == State.Away

  def isPlaying =
    state == State.Play

  def isFold =
    state == State.Fold

  def isAllIn =
    state == State.AllIn

  def isWaitingBB =
    state == State.WaitBB

  def isPostedBB =
    state == State.PostBB

  def canPlayNextDeal =
    isReady || isPlaying || isFold

  def isActive =
    state == State.Play || state == State.PostBB

  def notActive =
    state == State.Away || state == State.Idle || state == State.Auto

  def inPlay =
    state == State.Play || state == State.Bet

  //  def goesToShowdown =
  //    state == State.Bet || state == State.AllIn

  def inPot =
    inPlay || state == State.AllIn

}