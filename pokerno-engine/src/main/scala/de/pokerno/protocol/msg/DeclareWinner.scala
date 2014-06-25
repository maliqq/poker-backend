package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonInclude, JsonUnwrapped}

import de.pokerno.model.seat.Position

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclareWinner(
    @JsonUnwrapped position: Position,

    @JsonProperty amount: Decimal,
    
    @JsonProperty uncalled: Option[Boolean] = None
  ) extends GameEvent {}
