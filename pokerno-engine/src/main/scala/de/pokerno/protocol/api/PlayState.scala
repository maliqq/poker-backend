package de.pokerno.protocol.api

import de.pokerno.model._

import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonUnwrapped, JsonInclude}
import de.pokerno.gameplay
import de.pokerno.model

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class PlayState(
    ctx: gameplay.Context
  ) {
  @JsonUnwrapped val play = ctx.play
  @JsonUnwrapped val round = ctx.round
}

object RoomState {
  
  def apply(id: String, state: String, table: Table, variation: Variation, stake: Stake) =
    new RoomState(id, state, table, variation, stake)
  
  def apply(ctx: gameplay.Context, state: String) =
    new RoomState(ctx.id, state, ctx.table, ctx.variation, ctx.stake, new PlayState(ctx))
  
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class RoomState(
    @JsonProperty val id: String, // room id
    @JsonProperty val state: String, // room state
    @JsonProperty val table: Table, // room table seating
    @JsonProperty val variation: Variation, // room variation
    @JsonProperty val stake: Stake, // room stake
    @JsonProperty val play: PlayState = null // play state
) {
  
}
