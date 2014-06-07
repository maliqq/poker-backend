package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.pokerno.model

sealed case class DeclareStart(
    
    @JsonProperty table: model.Table,

    @JsonProperty variation: model.Variation,

    @JsonProperty stake: model.Stake

) extends GameEvent {
    
    @JsonProperty var play: Option[PlayState] = None
    
    @JsonSerialize(converter=classOf[Cards2Binary]) @JsonProperty var pocket: Cards = null 

}
