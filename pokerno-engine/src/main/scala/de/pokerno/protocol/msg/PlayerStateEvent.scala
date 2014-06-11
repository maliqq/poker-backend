package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}
import de.pokerno.model.{Seat, Position}

abstract class PlayerStateEvent extends GameEvent {
  @JsonUnwrapped val position: Position
}

object PlayerJoin {
  def apply(seat: Seat): PlayerJoin = PlayerJoin(seat, seat.stackAmount)
}

sealed case class PlayerJoin(
    override val position: Position,
    @JsonProperty amount: Decimal
) extends PlayerStateEvent {}

sealed case class PlayerLeave(
    override val position: Position
) extends PlayerStateEvent {}

sealed case class PlayerOffline(
    override val position: Position
) extends PlayerStateEvent {}

sealed case class PlayerOnline(
    override val position: Position
) extends PlayerStateEvent {}

sealed case class PlayerSitOut(
    override val position: Position
) extends PlayerStateEvent {}

sealed case class PlayerComeBack(
    override val position: Position
) extends PlayerStateEvent {}
