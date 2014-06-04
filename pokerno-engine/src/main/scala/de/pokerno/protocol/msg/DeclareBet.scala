package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.Bet
import com.fasterxml.jackson.annotation.{JsonInclude, JsonUnwrapped}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclareBet(
    @JsonProperty pos: Int,
    
    @JsonProperty player: Player,
    
    @JsonUnwrapped bet: Bet
  ) extends GameEvent {
  @JsonProperty var timeout: Option[Boolean] = None
}
