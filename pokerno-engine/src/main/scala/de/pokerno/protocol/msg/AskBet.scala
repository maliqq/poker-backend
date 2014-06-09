package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}
import de.pokerno.gameplay.betting.Acting

sealed case class AskBet(
    @JsonUnwrapped var acting: Acting
  ) extends GameEvent {}
