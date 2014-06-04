package de.pokerno.protocol.game_events

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model

sealed case class DeclareStart(
    
    @JsonProperty table: model.Table,

    @JsonProperty variation: model.Variation,

    @JsonProperty stake: model.Stake

) extends GameEvent {
    
    @JsonProperty var play: Option[PlayState] = None
    
    @JsonProperty var pocket: Option[Cards] = None 

}
