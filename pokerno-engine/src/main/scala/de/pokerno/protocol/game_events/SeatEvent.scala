package de.pokerno.protocol.game_events

import beans._
import de.pokerno.model.Seat

object SeatEventType extends Enumeration {
  val STATE, PRESENCE = Value
}

sealed case class SeatEvent(
    @BeanProperty var `type`: SeatEventType.Value,
    @BeanProperty var pos: Int,
    @BeanProperty var state: Seat.State.Value = null,
    @BeanProperty var presence: Seat.Presence.Value = null
) extends GameEvent {}
