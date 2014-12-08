package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.pokerno.model
import de.pokerno.gameplay

object DeclareStart {
  def apply(ctx: gameplay.Context, state: String, player: Option[Player] = None): DeclareStart = {
    val start = DeclareStart(ctx.id, state, ctx.table, ctx.variation, ctx.stake, player)
    start.play = Some(PlayState(ctx))
    start
  }
}

sealed case class DeclareStart(
    @JsonProperty id: String,

    @JsonProperty state: String,
    
    @JsonProperty table: model.Table,

    @JsonProperty variation: model.Variation,

    @JsonProperty stake: model.Stake,
    
    @JsonProperty player: Option[model.Player]

) extends GameEvent {
    
    @JsonProperty var play: Option[PlayState] = None
    
    @JsonSerialize(converter=classOf[Cards2Binary]) @JsonProperty var pocket: Cards = null 

}
