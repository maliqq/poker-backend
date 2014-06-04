package de.pokerno.protocol.game_events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclarePot(
    @JsonProperty var pot: Decimal,

    @JsonProperty var side: Seq[Decimal] = Seq(),

    @JsonProperty var rake: Option[Decimal] = None
  ) extends GameEvent {}
