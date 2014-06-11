package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.Bet
import com.fasterxml.jackson.annotation.{JsonInclude, JsonUnwrapped}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclareBet(
    @JsonProperty pos: Int,
    
    @JsonProperty player: Player,
    
    @JsonProperty action: Bet,
    
    @JsonProperty timeout: Option[Boolean] = None
  ) extends GameEvent {
  
}
