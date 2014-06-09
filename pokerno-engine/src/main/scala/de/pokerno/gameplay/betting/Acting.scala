package de.pokerno.gameplay.betting

import math.{BigDecimal => Decimal}
import de.pokerno.model.{Player, Seat}

import com.fasterxml.jackson.annotation.{JsonProperty, JsonInclude}

@JsonInclude(JsonInclude.Include.NON_NULL)
case class Acting(
    @JsonProperty pos: Int,
    @JsonProperty player: Option[Player],
    @JsonProperty call: Decimal,
    @JsonProperty raise: Option[Tuple2[Decimal, Decimal]]
) {

}
