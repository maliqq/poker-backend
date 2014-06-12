package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty

import de.pokerno.gameplay

object DeclarePlayStart {
  def apply(ctx: gameplay.Context): DeclarePlayStart = DeclarePlayStart(PlayState(ctx))
}

sealed case class DeclarePlayStart(
    @JsonProperty play: PlayState
) extends GameEvent {}
