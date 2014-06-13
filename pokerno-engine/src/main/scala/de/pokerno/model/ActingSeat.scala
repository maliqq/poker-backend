package de.pokerno.model

import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty}
import math.{BigDecimal => Decimal}

object ActingSeat {
  implicit def seat2acting(seat: Seat): ActingSeat = ActingSeat(seat.pos, seat.player, seat.call, seat.raise)
  implicit def acting2position(acting: ActingSeat): Position = Position(acting.pos, acting.player)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class ActingSeat(
    @JsonProperty pos: Int,
    @JsonProperty player: Option[Player],
    @JsonProperty call: Option[Decimal],
    @JsonProperty raise: Option[Tuple2[Decimal, Decimal]]
)  {}
