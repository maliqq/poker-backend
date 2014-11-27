package de.pokerno.model.seat.impl

import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonIgnore, JsonInclude, JsonProperty, JsonPropertyOrder, JsonGetter}
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import de.pokerno.model.Seat.State
import de.pokerno.model.SeatStateRef

trait Transitions {
  class States[T](initial: T) {
    private val transitions = collection.mutable.HashMap[T, T]()
  }
}

trait States {

  import Callbacks._
  
  def stackAmount: Decimal
  
  protected var _state: State.Value
  @JsonScalaEnumeration(classOf[SeatStateRef]) @JsonProperty def state = _state

  def state_=(_new: State.Value) {
    val _old = _state
    if (_old != _new) {
      stateCallbacks.before(_old, _new)
      _state = _new
    }
    //stateCallbacks.on(_old, _state)
    //stateCallbacks.after(_old, _state)
  }
  
  @JsonIgnore protected val stateCallbacks = new Callbacks[State.Value]()

    
  def isEmpty       = state == State.Empty
  def isTaken       = state == State.Taken
  def isReserved    = state == State.Taken // or Reserved
  def isReady       = state == State.Ready
  def isAway        = state == State.Away
  def isIdle        = state == State.Idle
  def isSitOut      = state == State.Idle // or SitOut
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
  
  def taken()       = state = State.Taken
  def idle()        = state = State.Idle
  //def sitOut()      = state = State.Idle
  def away()        = state = State.Away

  def canPlay =
    isReady || isPlaying || isFolded

  // PLAY | BET | POST_BB | ALL_IN
  def isActive =
    isPlaying || isBetting || isPostingBB || isAllIn

  // FOLD | AWAY | IDLE | AUTO
  def notActive =
    isFolded || isAway || isIdle || isAuto 

  // PLAY | BET
  def canContribute =
    isPlaying || isBetting

  def inPlay = canContribute
    
  // PLAY | BET | ALLIN
  def canClaim =
    isPlaying || isBetting || isAllIn
  
  def inPot = canClaim

}