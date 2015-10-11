package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonUnwrapped, JsonInclude}
import de.pokerno.gameplay
import de.pokerno.model.Street

object PlayState {
  def apply(ctx: gameplay.Context) = new PlayState(ctx)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class PlayState(
    ctx: gameplay.Context
  ) {
  @JsonUnwrapped val play = ctx.play
  @JsonUnwrapped val round = ctx.round
}
