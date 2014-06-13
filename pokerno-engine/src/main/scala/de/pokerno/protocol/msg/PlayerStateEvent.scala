package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}
import de.pokerno.model.Seat
import de.pokerno.model.seat.{Sitting, Position}

abstract class PlayerStateEvent extends GameEvent {
  val position: Position
}

sealed case class PlayerJoin(
    @JsonUnwrapped override val position: Position,
    @JsonProperty amount: Decimal
) extends PlayerStateEvent {}

sealed case class PlayerLeave(
    @JsonUnwrapped override val position: Position
) extends PlayerStateEvent {}

sealed case class PlayerOffline(
    @JsonUnwrapped override val position: Position
) extends PlayerStateEvent {}

sealed case class PlayerOnline(
    @JsonUnwrapped override val position: Position
) extends PlayerStateEvent {}

sealed case class PlayerSitOut(
    @JsonUnwrapped override val position: Position
) extends PlayerStateEvent {}

sealed case class PlayerComeBack(
    @JsonUnwrapped override val position: Position
) extends PlayerStateEvent {}
