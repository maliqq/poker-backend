package de.pokerno.protocol.game_events

import beans._
import de.pokerno.model

sealed case class DeclareStart(
    
    @BeanProperty table: model.Table,

    @BeanProperty variation: model.Variation,

    @BeanProperty stake: model.Stake

) extends GameEvent {
    
    @BeanProperty var play: PlayState = null
    
    @BeanProperty var pocket: Option[Cards] = None 

}
