package de.pokerno.model

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
    val Reserved = Taken // alias
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
    val SitOut = state("sit-out")
    val Idle = SitOut // alias
    // disconnected
    val Off = state("off")
    val Away = Off
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
    pos: Int = -1
  ) {
  
  @JsonIgnore protected val _pos: Int = pos
  @JsonIgnore protected val log = LoggerFactory.getLogger(getClass)

}
