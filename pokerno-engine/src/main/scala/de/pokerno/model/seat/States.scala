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
  def stackAmount: Decimal
    
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
  
  private def ensureAllIn(newState: State.Value) = {
    state = if (stackAmount == 0) State.AllIn else newState
  }
  
  def playing()     = ensureAllIn(State.Play)
  def betting()     = ensureAllIn(State.Bet)
  def ready()       = ensureAllIn(State.Ready)
  
  def idle()        = state = State.Idle
  def away()        = state = State.Away

  def canPlay =
    isReady || isPlaying //|| isFolded

  // PLAY | POST_BB | ALL_IN
  def isActive =
    isPlaying || isPostingBB || isAllIn

  // AWAY | IDLE | AUTO
  def notActive =
    isAway || isIdle || isAuto

  // PLAY | BET
  def inPlay =
    isPlaying || isBetting

  // PLAY | BET | ALLIN
  def inPot =
    inPlay || isAllIn

}