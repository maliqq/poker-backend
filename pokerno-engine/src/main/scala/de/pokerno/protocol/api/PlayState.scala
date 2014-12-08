package de.pokerno.protocol.api

import de.pokerno.model._

import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonUnwrapped, JsonInclude}
import de.pokerno.gameplay
import de.pokerno.model

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class Play(
    ctx: gameplay.Context
  ) {
  @JsonUnwrapped val play = ctx.play
  @JsonUnwrapped val round = ctx.round
  @JsonUnwrapped val dealer = ctx.dealer
}

object PlayState {
  
  def apply(id: String, state: String, table: Table, variation: Variation, stake: Stake) =
    new PlayState(id, state, table, variation, stake)
  
  def apply(ctx: gameplay.Context, state: String) =
    new PlayState(ctx.id, state, ctx.table, ctx.variation, ctx.stake, new Play(ctx))
  
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class PlayState(
    @JsonProperty val id: String,
    @JsonProperty val state: String,
    @JsonProperty val table: Table,
    @JsonProperty val variation: Variation,
    @JsonProperty val stake: Stake,
    @JsonProperty val play: Play = null
) {
  
}
