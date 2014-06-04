package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class StakeChange(
    @JsonProperty bigBlind: Decimal,
    @JsonProperty smallBlind: Option[Decimal] = None,
    @JsonProperty ante: Option[Decimal] = None,
    @JsonProperty bringIn: Option[Decimal] = None
) extends GameEvent {}
