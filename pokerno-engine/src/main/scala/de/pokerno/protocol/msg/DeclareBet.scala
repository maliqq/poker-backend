package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.Bet
import com.fasterxml.jackson.annotation.{JsonInclude, JsonUnwrapped}
import de.pokerno.model.seat.impl.Position

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclareBet(
    @JsonUnwrapped position: Position,

    @JsonProperty action: Bet,
    
    @JsonProperty timeout: Option[Boolean] = None
  ) extends GameEvent {}
