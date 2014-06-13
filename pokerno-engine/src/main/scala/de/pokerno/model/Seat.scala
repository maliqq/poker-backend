package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

import org.slf4j.LoggerFactory
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonInclude, JsonAutoDetect, JsonPropertyOrder}
import de.pokerno.util.Colored._

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.fasterxml.jackson.core.`type`.TypeReference

class SeatStateRef extends TypeReference[Seat.State.type]

object Seat {
  object State extends Enumeration {
    def state(name: String) = new Val(nextId, name)
    //def state[T <: Trait](name: String) = new Val(nextId, name) with T

    // no player
    val Empty = state("empty")
    // reserved seat
    val Taken = state("taken")
    // waiting next deal
    val Ready = state("ready")
    // waiting big blind
    val WaitBB = state("wait-bb")
    // posting big blind
    val PostBB = state("post-bb")
    // playing in current deal
    val Play = state("play")
    // all-in in current deal
    val AllIn = state("all-in")
    // did bet
    val Bet = state("bet")
    // did fold
    val Fold = state("fold")
    // autoplay
    val Auto = state("auto")
    // sit-out
    val Idle = state("idle")
    // disconnected
    val Away = state("away")
  }

  object Presence extends Enumeration {
    val Online = new Val(nextId, "online")
    val Offline = new Val(nextId, "offline")
  }

  case class IsTaken() extends Exception("seat is taken")
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
abstract class Seat(
    pos: Int,
    initialState: Seat.State.Value = Seat.State.Empty 
    ) extends seat.Position(pos) {

  import Seat._
  import seat.Callbacks._

  @JsonIgnore protected val log = LoggerFactory.getLogger(getClass)
  
  @JsonIgnore protected var _state: State.Value = initialState
  def this(_state: Seat.State.Value) = this(-1, _state)
  
  // STATE
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

}
