package de.pokerno.protocol.game_events

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.gameplay
import de.pokerno.model.Street
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonUnwrapped, JsonInclude}

object PlayState {
  def apply(ctx: gameplay.Context) = new PlayState(ctx)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class PlayState(
    ctx: gameplay.Context
  ) {
  @JsonUnwrapped val play = ctx.play
  @JsonUnwrapped val round = ctx.round
  @JsonUnwrapped val dealer = ctx.dealer
}
