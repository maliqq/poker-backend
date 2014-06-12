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
    
  def isEmpty       = state == State.Empty
  def isTaken       = state == State.Taken
  def isReady       = state == State.Ready
  def isAway        = state == State.Away
  def isIdle        = state == State.Idle
  def isAuto        = state == State.Auto
  def isPostingBB   = state == State.PostBB
  def isWaitingBB   = state == State.WaitBB
  def isBetting     = state == State.Bet
  def isFolded      = state == State.Fold
  def isAllIn       = state == State.AllIn
  def isPlaying     = state == State.Play
  
  def playing()     = state = State.Play
  def idle()        = state = State.Idle
  def ready()       = state = if (total == 0) State.Idle else State.Ready
  def away()        = state = State.Away

  def canPlay =
    isReady || isPlaying // || isFold

  def isActive =
    isPlaying || isPostingBB

  def notActive =
    isAway || isIdle || isAuto

  def inPlay =
    isPlaying || isBetting

  def inPot =
    inPlay || isAllIn

}