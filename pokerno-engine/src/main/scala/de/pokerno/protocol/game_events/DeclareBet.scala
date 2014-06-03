package de.pokerno.protocol.game_events

import beans._
import de.pokerno.model.Bet
import com.fasterxml.jackson.annotation.{JsonInclude, JsonUnwrapped}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclareBet(
    @BeanProperty pos: Int,
    
    @BeanProperty player: Player,
    
    @JsonUnwrapped bet: Bet
  ) extends GameEvent {
  @BeanProperty var timeout: Option[Boolean] = None
}
